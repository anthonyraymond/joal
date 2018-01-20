package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public class TooManyAnnouncesFailedPayload implements MessagePayload {
    private final InfoHash infoHash;

    public TooManyAnnouncesFailedPayload(final TooManyAnnouncesFailedEvent event) {
        this.infoHash = event.getInfoHash();
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }
}
