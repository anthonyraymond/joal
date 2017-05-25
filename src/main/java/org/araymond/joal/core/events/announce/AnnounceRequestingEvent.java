package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnounceRequestingEvent {
    private final RequestEvent event;
    private final TorrentWithStats torrent;

    public AnnounceRequestingEvent(final RequestEvent event, final TorrentWithStats torrent) {
        Preconditions.checkNotNull(event, "RequestEvent cannot be null");
        Preconditions.checkNotNull(torrent, "TorrentWithStats cannot be null");
        this.event = event;
        this.torrent = torrent;
    }

    public RequestEvent getEvent() {
        return event;
    }

    public TorrentWithStats getTorrent() {
        return torrent;
    }
}
