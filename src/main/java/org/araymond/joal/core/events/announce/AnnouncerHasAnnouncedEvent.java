package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasAnnouncedEvent {
    private final TorrentWithStats torrent;
    private final int interval;
    private final int seeders;
    private final int leechers;

    public AnnouncerHasAnnouncedEvent(final TorrentWithStats torrent, final int interval, final int seeders, final int leechers) {
        Preconditions.checkNotNull(torrent, "TorrentWithStats cannot be null");

        this.torrent = torrent;
        this.interval = interval;
        this.seeders = seeders;
        this.leechers = leechers;
    }

    public TorrentWithStats getTorrent() {
        return torrent;
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
