package org.araymond.joal.core.ttorent.client;

import com.google.common.eventbus.EventBus;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.announce.AnnounceResponseListener;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.ttorent.client.announce.Announce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by raymo on 23/01/2017.
 */
public class Client implements Runnable, AnnounceResponseListener {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    // Delay between each upload stat update (this is not announce delay)
    private static final int UPDATE_DELAY = 10;

    private final JoalConfigProvider configProvider;
    private final MockedTorrent torrent;
    private ClientState state;
    private final Peer self;
    private int peerCount;
    private Thread thread;
    private boolean stop;
    private long seed;

    private final ConnectionHandler service;
    private final Announce announce;

    private final EventBus eventBus;
    private Timer seedShutdownTimer;
    private final Random random;

    public Client(final JoalConfigProvider configProvider, final InetAddress address, final MockedTorrent torrent, final BitTorrentClient bitTorrentClient) throws IOException {
        this.configProvider = configProvider;
        this.torrent = torrent;
        this.eventBus = new EventBus();
        this.state = ClientState.WAITING;

        final String id = bitTorrentClient.getPeerId();
        this.service = new ConnectionHandler(this.torrent, id, address);

        this.self = new Peer(
                this.service.getSocketAddress().getAddress().getHostAddress(),
                this.service.getSocketAddress().getPort(),
                ByteBuffer.wrap(id.getBytes(Torrent.BYTE_ENCODING))
        );

        // Initialize the announce request thread, and register ourselves to it as well.
        this.announce = new Announce(this.torrent, this.self, bitTorrentClient);
        this.announce.register(this);

        this.peerCount = 0;
        this.random = new Random();
    }

    public void addEventBusListener(final Object object) {
        this.eventBus.register(object);
    }

    public void removeEventBusListener(final Object object) {
        this.eventBus.unregister(object);
    }

    /**
     * Return the current state of this BitTorrent client.
     */
    public ClientState getState() {
        return this.state;
    }

    /**
     * Change this client's state and notify its observers.
     * <p>
     * <p>
     * If the state has changed, this client's observers will be notified.
     * </p>
     *
     * @param state The new client state.
     */
    private synchronized void setState(final ClientState state) {
        this.state = state;
    }

    /* AnnounceResponseListener implementation *************************/
    @Override
    public void handleAnnounceResponse(final int interval, final int complete, final int incomplete) {
        this.announce.setInterval(interval);
    }


    @Override
    public void handleDiscoveredPeers(final List<Peer> discoveredPeers) {
        if (discoveredPeers == null || discoveredPeers.isEmpty()) {
            logger.warn("0 peers found for this torrent, stop seeding this torrent.");
            this.eventBus.post(Event.ENCOUNTER_ZERO_PEER);
            this.peerCount = 0;
            this.stop(false);
            return;
        }

        logger.info("{} peers are currently leeching.", discoveredPeers.size());
        this.peerCount = discoveredPeers.size();
    }

    /* Runnable implementation *****************************************/
    @Override
    public void run() {
        logger.trace("Running Client");
        this.seed();
        this.announce.start();

        while (!this.stop) {
            try {
                Thread.sleep(Client.UPDATE_DELAY * 1000);

                if (peerCount > 0) {
                    final int randomUploadValueInKb = random.nextInt(
                            configProvider.get().getMaxUploadRate() - configProvider.get().getMinUploadRate()
                    ) + configProvider.get().getMinUploadRate();
                    // Translate to bytes and add some mor randomness
                    float randomUploadValueInBytes = (randomUploadValueInKb + random.nextFloat()) * 1024;
                    // then multiply by the time the thread was paused
                    randomUploadValueInBytes *= Client.UPDATE_DELAY;

                    this.torrent.addUploaded((long) randomUploadValueInBytes);
                }
            } catch (final InterruptedException ie) {
                logger.debug("BitTorrent main loop interrupted.");
            }
        }
        logger.trace("Client has exited loop, going to stop.");

        try {
            this.service.close();
        } catch (final IOException ioe) {
            logger.warn("Error while releasing bound channel: {}!", ioe.getMessage(), ioe);
        }
        this.announce.stop();
    }

    /**
     * Immediately but gracefully stop this client.
     */
    public void stop() {
        this.stop(true);
    }

    /**
     * Immediately but gracefully stop this client.
     *
     * @param wait Whether to wait for the client execution thread to complete
     *             or not. This allows for the client's state to be settled down in one of
     *             the <tt>DONE</tt> or <tt>ERROR</tt> states when this method returns.
     */
    public void stop(final boolean wait) {
        logger.trace("Call to stop Client");
        this.stop = true;

        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
            if (wait) {
                this.waitForCompletion();
            }
        }

        if (seedShutdownTimer != null) {
            seedShutdownTimer.cancel();
        }

        this.thread = null;
        logger.trace("Client has stopped.");
    }

    /**
     * Wait for downloading (and seeding, if requested) to complete.
     */
    public void waitForCompletion() {
        if (this.thread != null && this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException ie) {
                logger.error(ie.getMessage(), ie);
            }
        }
    }

    /**
     * Display information about the BitTorrent client state.
     * <p>
     * <p>
     * This emits an information line in the log about this client's state. It
     * includes the number of choked peers, number of connected peers, number
     * of known peers, information about the torrent availability and
     * completion and current transmission rates.
     * </p>
     */
    public synchronized void info() {
        logger.info("{}, {} peerCount (leechers). Stats {}/{} MB.",
                this.getState().name(),
                this.peerCount,
                String.format("%.2f", this.torrent.getDownloaded() / 1024.0 / 1024),
                String.format("%.2f", this.torrent.getUploaded() / 1024.0 / 1024)
        );
    }

    /**
     * Start the seeding period, if any.
     * <p>
     * <p>
     * This method is called when all the pieces of our torrent have been
     * retrieved. This may happen immediately after the client starts if the
     * torrent was already fully download or we are the initial seeder client.
     * </p>
     * <p>
     * <p>
     * When the download is complete, the client switches to seeding mode for
     * as long as requested in the <code>share()</code> call, if seeding was
     * requested. If not, the {@link com.turn.ttorrent.client.Client.ClientShutdown} will execute
     * immediately to stop the client's main loop.
     * </p>
     *
     * @see com.turn.ttorrent.client.Client.ClientShutdown
     */
    private synchronized void seed() {
        // Silently ignore if we're already seeding.
        if (ClientState.SEEDING == this.getState()) {
            return;
        }

        this.setState(ClientState.SEEDING);
        if (this.seed < 0) {
            logger.info("Seeding indefinetely...");
            return;
        }

        // In case seeding for 0 seconds we still need to schedule the task in order to call stop() from different thread to avoid deadlock
        seedShutdownTimer = new Timer();
        seedShutdownTimer.schedule(new ClientShutdown(this), this.seed * 1000);
    }

    /**
     * Download and share this client's torrent.
     *
     * @param seed Seed time in seconds after the download is complete. Pass
     *             <code>0</code> to immediately stop after downloading.
     */
    public synchronized void share(final int seed) {
        this.seed = seed;
        this.stop = false;

        if (this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("bt-client(" + this.self.getShortHexPeerId() + ")");
            this.thread.start();
        }
    }

    /**
     * Timer task to stop seeding.
     * <p>
     * <p>
     * This TimerTask will be called by a timer set after the download is
     * complete to stop seeding from this client after a certain amount of
     * requested seed time (might be 0 for immediate termination).
     * </p>
     * <p>
     * <p>
     * This task simply contains a reference to this client instance and calls
     * its <code>stop()</code> method to interrupt the client's main loop.
     * </p>
     *
     * @author mpetazzoni
     */
    public static class ClientShutdown extends TimerTask {

        private final Client client;

        public ClientShutdown(final Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.setState(ClientState.DONE);
            this.client.stop();
        }
    }

    public enum Event {
        ENCOUNTER_ZERO_PEER
    }

}
