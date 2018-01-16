package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;

public class SuccessfullyAnnounceEvent {
    private final InfoHash infoHash;
    private final RequestEvent event;
    private final int interval;

    public SuccessfullyAnnounceEvent(final InfoHash infoHash, final RequestEvent event, final int interval) {
        this.infoHash = infoHash;
        this.event = event;
        this.interval = interval;
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public RequestEvent getEvent() {
        return event;
    }

    public int getInterval() {
        return interval;
    }
}
