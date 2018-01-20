package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public class SuccessfullyAnnouncePayload implements MessagePayload {
    private final InfoHash infoHash;
    private final int interval;
    private final RequestEvent requestEvent;

    public SuccessfullyAnnouncePayload(final SuccessfullyAnnounceEvent event) {
        this.infoHash = event.getInfoHash();
        this.interval = event.getInterval();
        this.requestEvent = event.getEvent();
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public int getInterval() {
        return interval;
    }
}
