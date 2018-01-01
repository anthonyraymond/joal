package org.araymond.joal.core.events.newd.announcer;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public class AnnouncerHasStarted {
    private final InfoHash infoHash;
    private final int interval;

    public AnnouncerHasStarted(final InfoHash infoHash, final int interval) {
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
