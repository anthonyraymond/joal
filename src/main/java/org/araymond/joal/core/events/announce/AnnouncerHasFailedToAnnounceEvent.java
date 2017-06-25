package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasFailedToAnnounceEvent {
    private final RequestEvent event;
    private final TorrentWithStats torrent;
    private final String error;

    public AnnouncerHasFailedToAnnounceEvent(final RequestEvent event, final TorrentWithStats torrent, final String error) {
        Preconditions.checkNotNull(event, "RequestEvent cannot be null");
        Preconditions.checkNotNull(torrent, "TorrentWithStats cannot be null");
        Preconditions.checkArgument(!StringUtils.isBlank(error), "Error message cannot be null or empty.");

        this.event = event;
        this.torrent = torrent;
        this.error = error;
    }

    public RequestEvent getEvent() {
        return event;
    }

    public TorrentWithStats getTorrent() {
        return torrent;
    }

    public String getError() {
        return error;
    }
}
