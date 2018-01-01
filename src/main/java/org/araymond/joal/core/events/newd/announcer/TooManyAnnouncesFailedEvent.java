package org.araymond.joal.core.events.newd.announcer;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public class TooManyAnnouncesFailedEvent {
    private final InfoHash infoHash;

    public TooManyAnnouncesFailedEvent(final InfoHash infoHash) {
        this.infoHash = infoHash;
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }
}
