package org.araymond.joal.core.events.announce;

import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;

public class TooManyAnnouncesFailedEvent {
    private final AnnouncerFacade announcerFacade;

    public TooManyAnnouncesFailedEvent(final AnnouncerFacade announcerFacade) {
        this.announcerFacade = announcerFacade;
    }

    public AnnouncerFacade getAnnouncerFacade() {
        return announcerFacade;
    }
}
