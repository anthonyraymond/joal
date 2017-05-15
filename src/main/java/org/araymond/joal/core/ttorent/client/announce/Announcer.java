package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import org.apache.commons.lang3.NotImplementedException;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.events.SomethingHasFuckedUp;
import org.araymond.joal.core.torrent.TorrentWithStats;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.*;

import static com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage;

/**
 * Created by raymo on 23/01/2017.
 */
public class Announcer implements Runnable, NewAnnounceResponseListener {

    protected static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    private final TorrentWithStats torrent;
    private final Peer peer;
    private final ApplicationEventPublisher publisher;

    /**
     * The tiers of tracker clients matching the tracker URIs defined in the
     * torrent.
     */
    private final List<List<TrackerClient>> clients;
    private final Set<TrackerClient> allClients;

    /**
     * Announce thread and control.
     */
    private Thread thread;
    private boolean stop;
    private boolean forceStop;

    /**
     * Announce interval.
     */
    private int interval;

    private int currentTier;
    private int currentClient;
    private final List<AnnouncerEventListener> eventListeners;

    /**
     * Initialize the base announce class members for the announcer.
     *
     * @param torrent The torrent we're announcing about.
     * @param peer    Our peer specification.
     */
    public Announcer(final MockedTorrent torrent, final Peer peer, final BitTorrentClient bitTorrentClient, final ApplicationEventPublisher publisher) {
        this.torrent = new TorrentWithStats(torrent);
        this.peer = peer;
        this.publisher = publisher;
        this.clients = new ArrayList<>();
        this.allClients = new HashSet<>();
        this.eventListeners = new ArrayList<>();

        /*
         * Build the tiered structure of tracker clients mapping to the
         * trackers of the torrent.
         */
        for (final List<URI> tier : this.torrent.getTorrent().getAnnounceList()) {
            final List<TrackerClient> tierClients = new ArrayList<>();
            for (final URI tracker : tier) {
                try {
                    final TrackerClient client = this.createTrackerClient(this.torrent, this.peer, tracker, bitTorrentClient);

                    tierClients.add(client);
                    this.allClients.add(client);
                } catch (final Exception e) {
                    logger.warn("Will not announce on {}: {}!",
                            tracker,
                            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
                    );
                }
            }

            // Shuffle the list of tracker clients once on creation.
            Collections.shuffle(tierClients);

            // Tier is guaranteed to be non-empty by
            // Torrent#parseAnnounceInformation(), so we can add it safely.
            clients.add(tierClients);
        }

        this.thread = null;
        this.currentTier = 0;
        this.currentClient = 0;

        this.register(this);

        logger.debug("Initialized announce sub-system with {} trackers on {}.",
                new Object[]{this.torrent.getTorrent().getTrackerCount(), torrent});
    }

    /**
     * Register a new announce response listener.
     *
     * @param listener The listener to register on this announcer events.
     */
    private void register(final NewAnnounceResponseListener listener) {
        for (final TrackerClient client : this.allClients) {
            client.register(listener);
        }
    }

    @Override
    public void handleAnnounceResponse(final TorrentWithStats torrent, final int interval, final int complete, final int incomplete) {
        this.setInterval(interval);
    }

    @Override
    public void handleDiscoveredPeers(final TorrentWithStats torrent, final List<Peer> peers) {
        if (peers == null || peers.isEmpty()) {
            this.eventListeners.forEach(listener -> listener.onNoMoreLeecherForTorrent(this, torrent.getTorrent()));
        }
    }

    public void registerEventListener(final AnnouncerEventListener client) {
        this.eventListeners.add(client);
    }

    /**
     * Start the announce request thread.
     */
    public void start() {
        this.stop = false;
        this.forceStop = false;

        if (this.clients.size() > 0 && (this.thread == null || !this.thread.isAlive())) {
            this.thread = new Thread(this);
            this.thread.setName("bt-announce(" + this.peer.getShortHexPeerId() + ")");
            this.thread.start();
            this.thread.setUncaughtExceptionHandler((thread, ex) ->
                    publisher.publishEvent(new SomethingHasFuckedUp(ex))
            );
        }
    }

    /**
     * Stop the announce thread.
     * <p>
     * <p>
     * One last 'stopped' announce event might be sent to the tracker to
     * announce we're going away, depending on the implementation.
     * </p>
     */
    public void stop() {
        logger.debug("Call to stop Announcer");
        this.stop = true;

        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();

            for (final TrackerClient client : this.allClients) {
                client.close();
            }

            try {
                this.thread.join();
            } catch (final InterruptedException ignored) {
            }
        }

        this.thread = null;
        logger.debug("Announcer stopped");
    }

    /**
     * Set the announce interval.
     */
    public void setInterval(final int interval) {
        if (interval <= 0) {
            this.stop(true);
            return;
        }

        if (this.interval == interval) {
            return;
        }

        logger.debug("Setting announce interval to {}s per tracker request.", interval);
        this.interval = interval;
    }

    /**
     * Main announce loop.
     * <p>
     * <p>
     * The announce thread starts by making the initial 'started' announce
     * request to register on the tracker and get the announce interval value.
     * Subsequent announce requests are ordinary, event-less, periodic requests
     * for peers.
     * </p>
     * <p>
     * <p>
     * Unless forcefully stopped, the announce thread will terminate by sending
     * a 'stopped' announce request before stopping.
     * </p>
     */
    @Override
    public void run() {
        logger.debug("Starting announce loop...");

        // Set an initial announce interval to 5 seconds. This will be updated
        // in real-time by the tracker's responses to our announce requests.
        this.interval = 5;

        AnnounceRequestMessage.RequestEvent event = AnnounceRequestMessage.RequestEvent.STARTED;

        while (!this.stop) {
            try {
                this.getCurrentTrackerClient().announce(event, false);
                for (final AnnouncerEventListener listener : this.eventListeners) {
                    listener.onAnnounceRequesting(event, this.torrent.getUploaded(), this.torrent.getDownloaded(), this.torrent.getLeft());
                }
                this.promoteCurrentTrackerClient();
                event = AnnounceRequestMessage.RequestEvent.NONE;
            } catch (final AnnounceException ae) {
                logger.warn("Exception in announce", ae.getMessage());

                try {
                    // TODO : may need a better way to handle exception here, like "retry twice on fail then move to next"
                    this.moveToNextTrackerClient();
                } catch (final AnnounceException e) {
                    logger.error("Unable to move to the next tracker client: {}", e.getMessage());
                }
            }

            try {
                if (!this.stop) {
                    // If the thread was killed by himslef (in case no more leecher) the stop flag will be set.
                    // But the thread will have been interrupted already.
                    Thread.sleep(this.interval * 1000);
                }
            } catch (final InterruptedException ie) {
                // Ignore
            }
        }

        logger.debug("Exited announce loop.");

        if (!this.forceStop) {
            // Send the final 'stopped' event to the tracker after a little while.
            event = AnnounceRequestMessage.RequestEvent.STOPPED;

            try {
                this.getCurrentTrackerClient().announce(event, true);
            } catch (final AnnounceException ae) {
                logger.warn(ae.getMessage());
            }
        }
        this.eventListeners.forEach(listener -> listener.onAnnouncerStop(this, this.torrent.getTorrent()));
    }

    /**
     * Create a {@link TrackerClient} annoucing to the given tracker address.
     *
     * @param torrent          The torrent the tracker client will be announcing for.
     * @param peer             The peer the tracker client will announce on behalf of.
     * @param tracker          The tracker address as a {@link URI}.
     * @param bitTorrentClient
     * @throws UnknownHostException    If the tracker address is invalid.
     * @throws UnknownServiceException If the tracker protocol is not supported.
     */
    private TrackerClient createTrackerClient(final TorrentWithStats torrent, final Peer peer, final URI tracker, final BitTorrentClient bitTorrentClient) throws UnknownHostException, UnknownServiceException {
        final String scheme = tracker.getScheme();

        if ("http".equals(scheme) || "https".equals(scheme)) {
            return new HTTPTrackerClient(torrent, peer, tracker, bitTorrentClient);
        } else if ("udp".equals(scheme)) {
            // FIXME: implement UDPTracketClient
            throw new NotImplementedException("UDP Client not implemented yet.");
            //return new UDPTrackerClient(torrent, peer, tracker);
        }

        throw new UnknownServiceException("Unsupported announce scheme: " + scheme + "!");
    }

    /**
     * Returns the current tracker client used for announces.
     *
     * @throws AnnounceException When the current announce tier isn't defined
     *                           in the torrent.
     */
    public TrackerClient getCurrentTrackerClient() throws AnnounceException {
        if ((this.currentTier >= this.clients.size()) || (this.currentClient >= this.clients.get(this.currentTier).size())) {
            throw new AnnounceException("Current tier or client isn't available");
        }

        return this.clients
                .get(this.currentTier)
                .get(this.currentClient);
    }

    /**
     * Promote the current tracker client to the top of its tier.
     * <p>
     * <p>
     * As defined by BEP#0012, when communication with a tracker is successful,
     * it should be moved to the front of its tier.
     * </p>
     * <p>
     * <p>
     * The index of the currently used {@link TrackerClient} is reset to 0 to
     * reflect this change.
     * </p>
     *
     * @throws AnnounceException
     */
    private void promoteCurrentTrackerClient() throws AnnounceException {
        logger.trace("Promoting current tracker client for {} " + "(tier {}, position {} -> 0).",
                this.getCurrentTrackerClient().getTrackerURI(),
                this.currentTier,
                this.currentClient
        );

        Collections.swap(this.clients.get(this.currentTier), this.currentClient, 0);
        this.currentClient = 0;
    }

    /**
     * Move to the next tracker client.
     * <p>
     * <p>
     * If no more trackers are available in the current tier, move to the next
     * tier. If we were on the last tier, restart from the first tier.
     * </p>
     * <p>
     * <p>
     * By design no empty tier can be in the tracker list structure so we don't
     * need to check for empty tiers here.
     * </p>
     *
     * @throws AnnounceException
     */
    private void moveToNextTrackerClient() throws AnnounceException {
        int tier = this.currentTier;
        int client = this.currentClient + 1;

        if (client >= this.clients.get(tier).size()) {
            client = 0;

            tier++;

            if (tier >= this.clients.size()) {
                tier = 0;
            }
        }

        if (tier != this.currentTier || client != this.currentClient) {
            this.currentTier = tier;
            this.currentClient = client;

            logger.debug("Switched to tracker client for {} " + "(tier {}, position {}).",
                    this.getCurrentTrackerClient().getTrackerURI(),
                    this.currentTier,
                    this.currentClient
            );
        }
    }

    /**
     * Stop the announce thread.
     *
     * @param hard Whether to force stop the announce thread or not, i.e. not
     *             send the final 'stopped' announce request or not.
     */
    private void stop(final boolean hard) {
        this.forceStop = hard;
        this.stop();
    }
}
