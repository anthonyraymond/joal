package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.client.announce.AnnounceResponseListener;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.turn.ttorrent.common.protocol.TrackerMessage.*;

/**
 * Created by raymo on 23/01/2017.
 */
public abstract class TrackerClient {

    /**
     * The set of listeners to announce request answers.
     */
    private final Set<AnnounceResponseListener> listeners;

    protected final MockedTorrent torrent;
    protected final Peer peer;
    protected final URI tracker;

    public TrackerClient(final MockedTorrent torrent, final Peer peer, final URI tracker) {
        this.listeners = new HashSet<>();
        this.torrent = torrent;
        this.peer = peer;
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
     * @param event        The announce event type (can be AnnounceEvent.NONE for
     *                     periodic updates).
     * @param inhibitEvent Prevent event listeners from being notified.
     */
    public abstract void announce(AnnounceRequestMessage.RequestEvent event, boolean inhibitEvent) throws AnnounceException;

    /**
     * Close any opened announce connection.
     * <p>
     * <p>
     * This method is called by {@link Announce#stop()} to make sure all connections
     * are correctly closed when the announce thread is asked to stop.
     * </p>
     */
    protected void close() {
        // Do nothing by default, but can be overloaded.
    }

    /**
     * Formats an announce event into a usable string.
     */
    protected String formatAnnounceEvent(final AnnounceRequestMessage.RequestEvent event) {
        if (AnnounceRequestMessage.RequestEvent.NONE == event) {
            return "";
        }
        return String.format(" %s", event.name());
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
     * @param message       The incoming {@link TrackerMessage}.
     * @param inhibitEvents Whether or not to prevent events from being fired.
     */
    protected void handleTrackerAnnounceResponse(final TrackerMessage message,
                                                 final boolean inhibitEvents) throws AnnounceException {
        if (message instanceof ErrorMessage) {
            final ErrorMessage error = (ErrorMessage) message;
            throw new AnnounceException(error.getReason());
        }

        if (!(message instanceof AnnounceResponseMessage)) {
            throw new AnnounceException("Unexpected tracker message type " +
                    message.getType().name() + "!");
        }

        if (inhibitEvents) {
            return;
        }

        final AnnounceResponseMessage response = (AnnounceResponseMessage) message;
        this.fireAnnounceResponseEvent(
                response.getComplete(),
                response.getIncomplete(),
                response.getInterval()
        );
        this.fireDiscoveredPeersEvent(
                response.getPeers()
        );
    }

    /**
     * Fire the announce response event to all listeners.
     *
     * @param complete   The number of seeders on this torrent.
     * @param incomplete The number of leechers on this torrent.
     * @param interval   The announce interval requested by the tracker.
     */
    protected void fireAnnounceResponseEvent(final int complete, final int incomplete, final int interval) {
        for (final AnnounceResponseListener listener : this.listeners) {
            listener.handleAnnounceResponse(interval, complete, incomplete);
        }
    }

    /**
     * Fire the new peer discovery event to all listeners.
     *
     * @param peers The list of peers discovered.
     */
    protected void fireDiscoveredPeersEvent(final List<Peer> peers) {
        for (final AnnounceResponseListener listener : this.listeners) {
            listener.handleDiscoveredPeers(peers);
        }
    }

}
