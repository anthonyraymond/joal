package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResponseListener;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.turn.ttorrent.common.protocol.TrackerMessage.*;

/**
 * Created by raymo on 23/01/2017.
 */
public abstract class TrackerClient {
    private static final Logger logger = LoggerFactory.getLogger(TrackerClient.class);

    /**
     * The set of listeners to announce request answers.
     */
    private final Set<AnnounceResponseListener> listeners;

    protected final TorrentWithStats torrent;
    protected final ConnectionHandler connectionHandler;
    protected final URI tracker;

    public TrackerClient(final TorrentWithStats torrent, final ConnectionHandler connectionHandler, final URI tracker) {
        Preconditions.checkNotNull(torrent, "Torrent must not be null.");
        Preconditions.checkNotNull(connectionHandler, "ConnectionHandler must not be null.");
        Preconditions.checkNotNull(tracker, "URI must not be null.");

        this.listeners = new HashSet<>();
        this.torrent = torrent;
        this.connectionHandler = connectionHandler;
        this.tracker = tracker;
    }

    /**
     * Register a new announce response listener.
     *
     * @param listener The listener to register on this announcer events.
     */
    public void register(final AnnounceResponseListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Returns the URI this tracker clients connects to.
     */
    public URI getTrackerURI() {
        return this.tracker;
    }

    protected abstract TrackerMessage makeCallAndGetResponseAsByteBuffer(final AnnounceRequestMessage.RequestEvent event) throws AnnounceException;

    /**
     * Build, send and process a tracker announce request.
     * <p>
     * <p>
     * This function first builds an announce request for the specified event
     * with all the required parameters. Then, the request is made to the
     * tracker and the response analyzed.
     * </p>
     * <p>
     * <p>
     * All registered {@link AnnounceResponseListener} objects are then fired
     * with the decoded payload.
     * </p>
     *
     * @param event The announce event type (can be AnnounceEvent.NONE for
     *              periodic updates).
     */
    public final void announce(final AnnounceRequestMessage.RequestEvent event) throws AnnounceException {
        logger.debug("Announcing {} to tracker with {}U/{}D/{}L bytes...",
                this.formatAnnounceEvent(event),
                this.torrent.getUploaded(),
                this.torrent.getDownloaded(),
                this.torrent.getLeft());

        final TrackerMessage responseMessage = makeCallAndGetResponseAsByteBuffer(event);
        this.handleTrackerAnnounceResponse(responseMessage);
    }

    /**
     * Close any opened announce connection.
     * <p>
     * <p>
     * This method is called by {@link Announcer#stop()} to make sure all connections
     * are correctly closed when the announce thread is asked to stop.
     * </p>
     */
    public void close() {
        // Do nothing by default, but can be overloaded.
    }

    /**
     * Formats an announce event into a usable string.
     */
    protected String formatAnnounceEvent(final AnnounceRequestMessage.RequestEvent event) {
        if (AnnounceRequestMessage.RequestEvent.NONE == event) {
            return "";
        }
        return String.format("%s", event.name());
    }

    /**
     * Handle the announce response from the tracker.
     * <p>
     * <p>
     * Analyzes the response from the tracker and acts on it. If the response
     * is an error, it is logged. Otherwise, the announce response is used
     * to fire the corresponding announce and peer events to all announce
     * listeners.
     * </p>
     *
     * @param message The incoming {@link TrackerMessage}.
     */
    protected void handleTrackerAnnounceResponse(final TrackerMessage message) throws AnnounceException {
        if (message instanceof ErrorMessage) {
            final ErrorMessage error = (ErrorMessage) message;
            throw new AnnounceException(error.getReason());
        }

        if (!(message instanceof AnnounceResponseMessage)) {
            throw new AnnounceException("Unexpected tracker message type " + message.getType().name() + "!");
        }

        final AnnounceResponseMessage response = (AnnounceResponseMessage) message;
        this.torrent.setInterval(response.getInterval());
        this.torrent.setSeeders(response.getComplete());
        this.torrent.setLeechers(response.getIncomplete());
        this.fireAnnounceResponseEvent();
        this.fireDiscoveredPeersEvent(
                response.getPeers()
        );
    }

    private void fireAnnounceResponseEvent() {
        for (final AnnounceResponseListener listener : this.listeners) {
            listener.handleAnnounceResponse(this.torrent);
        }
    }

    /**
     * Fire the new peer discovery event to all listeners.
     *
     * @param peers The list of peers discovered.
     */
    private void fireDiscoveredPeersEvent(final List<Peer> peers) {
        for (final AnnounceResponseListener listener : this.listeners) {
            listener.handleDiscoveredPeers(this.torrent, peers);
        }
    }

}
