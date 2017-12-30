package org.araymond.joal.core.ttorrent.client.announcer.thread;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;

public class AnnounceThreadFactory {

    public AnnounceRunnable createStartAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.STARTED, announcer, callback);
    }

    public AnnounceRunnable createRegularAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.NONE, announcer, callback);
    }

    public AnnounceRunnable createStopAnnounceThread(final Announcer announcer, final AnnounceThreadCallback callback) {
        return this.createAnnounceThread(RequestEvent.STOPPED, announcer, callback);
    }

    private AnnounceRunnable createAnnounceThread(final RequestEvent event, final Announcer announcer, final AnnounceThreadCallback callback) {
        return new AnnounceRunnable(event, announcer, callback);
    }

}
