package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;

public final class AnnounceRequest {

    private final Announcer announcer;
    private final RequestEvent event;

    private AnnounceRequest(final Announcer announcer, final RequestEvent event) {
        this.announcer = announcer;
        this.event = event;
    }

    public static AnnounceRequest createStart(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.STARTED);
    }

    public static AnnounceRequest createRegular(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.NONE);
    }

    public static AnnounceRequest createStop(final Announcer announcer) {
        return new AnnounceRequest(announcer, RequestEvent.STOPPED);
    }

    public Announcer getAnnouncer() {
        return this.announcer;
    }

    public RequestEvent getEvent() {
        return this.event;
    }
}
