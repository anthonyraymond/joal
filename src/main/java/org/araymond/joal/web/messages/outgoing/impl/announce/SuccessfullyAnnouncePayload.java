package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;

public class SuccessfullyAnnouncePayload extends AnnouncePayload {
    private final int interval;
    private final RequestEvent requestEvent;
    private final LocalDateTime dateTime;

    public SuccessfullyAnnouncePayload(final SuccessfullyAnnounceEvent event) {
        super(event.getInfoHash());
        this.interval = event.getInterval();
        this.requestEvent = event.getEvent();
        this.dateTime = LocalDateTime.now();
    }

    public int getInterval() {
        return interval;
    }

    public RequestEvent getRequestEvent() {
        return requestEvent;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
