package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedPayload extends AnnouncePayload {

    private final int interval;
    private final int seeders;
    private final int leechers;

    public AnnouncerHasAnnouncedPayload(final TorrentWithStats torrent, final int interval, final int seeders, final int leechers) {
        super(torrent);
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
