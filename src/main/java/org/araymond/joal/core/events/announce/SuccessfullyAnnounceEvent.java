package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

public class SuccessfullyAnnounceEvent {
    private final AnnouncerFacade announcerFacade;
    private final RequestEvent event;

    public SuccessfullyAnnounceEvent(final AnnouncerFacade announcerFacade, final RequestEvent event) {
        this.announcerFacade = announcerFacade;
        this.event = event;
    }

    public AnnouncerFacade getAnnouncerFacade() {
        return announcerFacade;
    }

    public RequestEvent getEvent() {
        return event;
    }
}
