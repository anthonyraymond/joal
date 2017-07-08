package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.announce.Announcer;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerWillAnnounceEvent extends AnnouncerEvent {
    private final RequestEvent event;

    public AnnouncerWillAnnounceEvent(final Announcer announcer, final RequestEvent event) {
        super(announcer);
        Preconditions.checkNotNull(event, "RequestEvent must not be null");

        this.event = event;
    }

    public RequestEvent getEvent() {
        return event;
    }

}
