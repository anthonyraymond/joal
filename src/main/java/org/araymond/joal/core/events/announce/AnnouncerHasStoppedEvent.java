package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasStoppedEvent {
    private final TorrentWithStats torrent;

    public AnnouncerHasStoppedEvent(final TorrentWithStats torrent) {
        Preconditions.checkNotNull(torrent, "TorrentWithStats cannot be null");
        this.torrent = torrent;
    }

    public TorrentWithStats getTorrent() {
        return torrent;
    }
}
