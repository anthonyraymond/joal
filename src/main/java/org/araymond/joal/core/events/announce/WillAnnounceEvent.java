package org.araymond.joal.core.events.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;

public class WillAnnounceEvent {
    private final InfoHash infoHash;
    private final RequestEvent event;

    public WillAnnounceEvent(final InfoHash infoHash, final RequestEvent event) {
        this.infoHash = infoHash;
        this.event = event;
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public RequestEvent getEvent() {
        return event;
    }
}
