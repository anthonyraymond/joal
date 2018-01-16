package org.araymond.joal.core.events.old.announce;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public class FailedToAnnounceEvent {
    private final InfoHash infoHash;
    private final int interval;

    public FailedToAnnounceEvent(final InfoHash infoHash, final int interval) {
        this.infoHash = infoHash;
        this.interval = interval;
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public int getInterval() {
        return interval;
    }
}
