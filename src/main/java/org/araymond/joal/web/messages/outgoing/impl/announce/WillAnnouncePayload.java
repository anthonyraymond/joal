package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;

public class WillAnnouncePayload extends AnnouncePayload {
    public WillAnnouncePayload(final WillAnnounceEvent event) {
        super(event.getAnnouncerFacade());
    }
}
