package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

public class FailedToAnnounceEvent {
    private final AnnouncerFacade announcerFacade;
    private final RequestEvent event;
    private final String errMessage;

    public FailedToAnnounceEvent(final AnnouncerFacade announcerFacade, final RequestEvent event, final String errMessage) {
        this.announcerFacade = announcerFacade;
        this.event = event;
        this.errMessage = errMessage;
    }

    public AnnouncerFacade getAnnouncerFacade() {
        return announcerFacade;
    }

    public RequestEvent getEvent() {
        return event;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
