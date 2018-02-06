package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;

public class WillAnnouncePayload extends AnnouncePayload {
    private final LocalDateTime dateTime;

    public WillAnnouncePayload(final WillAnnounceEvent event) {
        super(event.getInfoHash());
        this.dateTime = LocalDateTime.now();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
