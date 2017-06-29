package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasStartedEvent {
    private final TorrentWithStats torrent;

    public AnnouncerHasStartedEvent(final TorrentWithStats torrent) {
        Preconditions.checkNotNull(torrent, "TorrentWithStats cannot be null");
        this.torrent = torrent;
    }

    public TorrentWithStats getTorrent() {
        return torrent;
    }
}
