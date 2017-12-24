package org.araymond.joal.core.ttorrent.client.announcer.thread.factory;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.announcer.Announcer;

public class AnnounceThreadFactory {

    public Runnable createStartAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.STARTED, announcer, callback);
    }

    public Runnable createRegularAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.NONE, announcer, callback);
    }

    public Runnable createStopAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.STOPPED, announcer, callback);
    }

    private Runnable createAnnounceThread(final RequestEvent event, final Announcer announcer, final AnnounceThreadCallback callback) {
        return () -> {
            try {
                callback.onSuccess(event, announcer, announcer.announce(event));
            } catch (final AnnounceException e) {
                callback.onFailure(event, announcer, e);
            }
        };
    }

}
