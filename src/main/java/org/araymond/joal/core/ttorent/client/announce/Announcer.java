package org.araymond.joal.core.ttorent.client.announce;

import com.google.common.collect.EvictingQueue;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.events.SomethingHasFuckedUpEvent;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.announce.tracker.TrackerClient;
import org.araymond.joal.core.ttorent.client.announce.tracker.TrackerClientProvider;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage;
import static org.araymond.joal.core.ttorent.client.announce.AnnounceResult.ErrorAnnounceResult;
import static org.araymond.joal.core.ttorent.client.announce.AnnounceResult.SuccessAnnounceResult;

/**
 * Created by raymo on 23/01/2017.
 */
public class Announcer implements Runnable, AnnounceResponseListener {

    protected static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    private final TorrentWithStats torrent;
    private final ApplicationEventPublisher publisher;
    private final TrackerClientProvider trackerClientProvider;
    private TrackerClient currentClient;

    private final EvictingQueue<AnnounceResult> announceHistory;
    private final List<AnnouncerEventListener> eventListeners;

    /**
     * Announce thread and control.
     */
    private Thread thread;
    private boolean stop;

    public Announcer(final MockedTorrent torrent, final ConnectionHandler connectionHandler, final BitTorrentClient bitTorrentClient, final ApplicationEventPublisher publisher) {
        this.announceHistory = EvictingQueue.create(3);
        this.torrent = new TorrentWithStats(torrent);
        this.publisher = publisher;
        this.eventListeners = new ArrayList<>();
        this.trackerClientProvider = new TrackerClientProvider(this.torrent, connectionHandler, bitTorrentClient);
        this.moveToNextTrackerClient();

        this.thread = null;

        if (logger.isDebugEnabled()) {
            logger.debug("Initialized announce sub-system with {} trackers on {}.", new Object[]{this.torrent.getTorrent().getTrackerCount(), torrent});
        }
    }

    public Collection<AnnounceResult> getAnnounceHistory() {
        return Collections.unmodifiableCollection(announceHistory);
    }

    @Override
    public void handleAnnounceResponse(final TorrentWithStats torrent) {
        if (this.stop) {
            return;
        }

        logger.info(
                "Peers discovery for torrent {}: {} leechers & {} seeders",
                torrent.getTorrent().getName(),
                torrent.getLeechers(),
                torrent.getSeeders()
        );

        final AnnounceResult announceResult = new SuccessAnnounceResult();
        this.announceHistory.add(announceResult);
        for (final AnnouncerEventListener listener : this.eventListeners) {
            listener.onAnnounceSuccess(this);
        }

        if (this.torrent.getLeechers() == 0) {
            this.eventListeners.forEach(listener -> listener.onNoMoreLeecherForTorrent(this, torrent));
        }
    }

    @Override
    public void handleDiscoveredPeers(final TorrentWithStats torrent, final List<Peer> peers) {
        // list of all peers, containing both seeders, leechers and yourself
    }

    public void registerEventListener(final AnnouncerEventListener client) {
        this.eventListeners.add(client);
    }


    /**
     * Start the announce request thread.
     */
    public void start() {
        this.stop = false;

        if (this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("bt-announce(" + this.torrent.getTorrent().getHexInfoHash() + ")");
            this.thread.start();
            this.thread.setUncaughtExceptionHandler((thread, ex) ->
                    publisher.publishEvent(new SomethingHasFuckedUpEvent(ex))
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
        if (logger.isDebugEnabled()) {
            logger.debug("Call to stop Announcer for torrent {}", torrent.getTorrent().getName());
        }
        this.stop = true;

        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();

            try {
                this.thread.join();
            } catch (final InterruptedException ignored) {
            }
        }

        this.thread = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Announcer stopped for torrent {}", torrent.getTorrent().getName());
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug("Starting announce loop for torrent {}.", torrent.getTorrent().getName());
        }

        AnnounceRequestMessage.RequestEvent event = AnnounceRequestMessage.RequestEvent.STARTED;
        eventListeners.forEach(listener -> listener.onAnnouncerStart(this));

        int successiveAnnounceErrors = 0;
        while (!this.stop) {
            try {
                for (final AnnouncerEventListener listener : this.eventListeners) {
                    listener.onAnnouncerWillAnnounce(event, this);
                }
                this.currentClient.announce(event);

                event = AnnounceRequestMessage.RequestEvent.NONE;

                successiveAnnounceErrors = 0;
            } catch (final AnnounceException ae) {
                logger.warn("Exception in announce for torrent {}", torrent.getTorrent().getName(), ae);

                final AnnounceResult announceResult = new ErrorAnnounceResult(ae.getMessage());
                announceHistory.add(announceResult);
                for (final AnnouncerEventListener listener : this.eventListeners) {
                    listener.onAnnounceFail(this, ae.getMessage());
                }

                ++successiveAnnounceErrors;
                if (successiveAnnounceErrors >= this.trackerClientProvider.addressesCount() && successiveAnnounceErrors > 5) {
                    logger.warn(
                            "Failed {} consecutive announce for torrent {}, this torrent will be deleted.",
                            successiveAnnounceErrors,
                            torrent.getTorrent().getName()
                    );
                    // If announce failed at least 5 times. And at least as much as the number of tracker clients
                    // it is likely that the torrent is not registered or the tracker is dead.
                    this.eventListeners.forEach(listener -> listener.onShouldDeleteTorrent(this, torrent));
                }

                this.moveToNextTrackerClient();
            }

            try {
                if (!this.stop) {
                    // If the thread was killed by himself (in case no more leechers) the stop flag will be set.
                    // But the thread will have been interrupted already.
                    Thread.sleep(this.torrent.getInterval() * 1000);
                }
            } catch (final InterruptedException ie) {
                // Ignore
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Exited announce loop for torrent {}.", torrent.getTorrent().getName());
        }

        // Send the final 'stopped' event to the tracker after a little while.
        event = AnnounceRequestMessage.RequestEvent.STOPPED;

        try {
            for (final AnnouncerEventListener listener : this.eventListeners) {
                listener.onAnnouncerWillAnnounce(event, this);
            }

            this.currentClient.announce(event);
        } catch (final AnnounceException ae) {
            logger.warn("Error while announcing stop for torrent {}.", torrent.getTorrent().getName(), ae);

            final AnnounceResult announceResult = new ErrorAnnounceResult(ae.getMessage());
            this.announceHistory.add(announceResult);
            for (final AnnouncerEventListener listener : this.eventListeners) {
                listener.onAnnounceFail(this, ae.getMessage());
            }
        }
        this.eventListeners.forEach(listener -> listener.onAnnouncerStop(this));
    }

    public TorrentWithStats getSeedingTorrent() {
        return this.torrent;
    }

    public boolean isForTorrent(final MockedTorrent torrent) {
        return this.torrent.getTorrent().equals(torrent);
    }


    private void moveToNextTrackerClient() {
        if (this.currentClient != null) {
            this.currentClient.unregister(this);
            this.currentClient.close();
        }
        this.currentClient = trackerClientProvider.getNext();
        this.currentClient.register(this);
    }

}
