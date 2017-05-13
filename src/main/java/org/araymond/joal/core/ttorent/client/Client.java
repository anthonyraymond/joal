package org.araymond.joal.core.ttorent.client;

import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.announce.AnnounceResponseListener;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.NoMoreLeechers;
import org.araymond.joal.core.events.SomethingHasFuckedUp;
import org.araymond.joal.core.ttorent.client.announce.Announce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

/**
 * Created by raymo on 23/01/2017.
 */
public class Client implements Runnable, AnnounceResponseListener {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    // Delay between each upload stat update (this is not announce delay)
    private static final int UPDATE_DELAY = 10;

    private final JoalConfigProvider configProvider;
    private final MockedTorrent torrent;
    private final ApplicationEventPublisher publisher;
    private ClientState state;
    private final Peer self;
    private int peerCount;
    private Thread thread;
    private boolean stop;

    private final ConnectionHandler service;
    private final Announce announce;

    private final Random random;

    public Client(final JoalConfigProvider configProvider, final InetAddress address, final MockedTorrent torrent, final BitTorrentClient bitTorrentClient, final ApplicationEventPublisher publisher) throws IOException {
        this.configProvider = configProvider;
        this.torrent = torrent;
        this.publisher = publisher;
        this.state = ClientState.WAITING;

        this.service = new ConnectionHandler(address);

        final String id = bitTorrentClient.getPeerId();
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
        // TODO : remove discoveredPeers.clear();, this is for test purpose
        if (discoveredPeers == null || discoveredPeers.isEmpty()) {
            logger.info("{} peers are currently leeching.", 0);
            publisher.publishEvent(new NoMoreLeechers(torrent));
            this.peerCount = 0;
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

                final int randomUploadValueInKb = random.nextInt(
                        configProvider.get().getMaxUploadRate() - configProvider.get().getMinUploadRate()
                ) + configProvider.get().getMinUploadRate();
                // Translate to bytes and add some mor randomness
                float randomUploadValueInBytes = (randomUploadValueInKb + random.nextFloat()) * 1024;
                // then multiply by the time the thread was paused
                randomUploadValueInBytes *= Client.UPDATE_DELAY;

                this.torrent.addUploaded((long) randomUploadValueInBytes);
            } catch (final InterruptedException ie) {
                logger.debug("BitTorrent main loop interrupted.", ie);
            }
        }
        logger.debug("Client has exited loop.");

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
        this.setState(ClientState.WAITING);

        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
            if (wait) {
                this.waitForCompletion();
            }
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
        logger.info("Seeding...");
    }

    /**
     * Download and share this client's torrent.
     */
    public synchronized void share() {
        this.stop = false;

        if (this.thread != null && this.thread.isAlive()) {
            logger.warn("Client is already sharing, won't start share twice on the same client.");
        }
        this.thread = new Thread(this);
        this.thread.setName("bt-client(" + this.self.getShortHexPeerId() + ")");
        this.thread.setUncaughtExceptionHandler((thread, ex) -> {
            this.setState(ClientState.ERROR);
            try {
                this.service.close();
            } catch (final IOException ignore) {
            }
            this.announce.stop();
            this.publisher.publishEvent(new SomethingHasFuckedUp(ex));
        });
        this.thread.start();
    }

}
