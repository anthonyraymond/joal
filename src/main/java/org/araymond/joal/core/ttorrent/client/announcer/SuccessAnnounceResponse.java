package org.araymond.joal.core.ttorrent.client.announcer;

import org.araymond.joal.core.torrent.torrent.InfoHash;

public class SuccessAnnounceResponse {

    private final int interval;
    private final int seeders;
    private final int leechers;

    public SuccessAnnounceResponse(final int interval, final int seeders, final int leechers) {
        this.interval = interval;
        this.seeders = seeders;
        this.leechers = leechers;
    }

    public int getInterval() {
        return interval;
    }

    public int getSeeders() {
        return seeders;
    }

    public int getLeechers() {
        return leechers;
    }
}
