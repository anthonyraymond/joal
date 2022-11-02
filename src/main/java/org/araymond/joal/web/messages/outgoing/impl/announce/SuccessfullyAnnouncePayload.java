package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;

@Getter
public class SuccessfullyAnnouncePayload extends AnnouncePayload {
    private final RequestEvent requestEvent;

    public SuccessfullyAnnouncePayload(final SuccessfullyAnnounceEvent event) {
        super(event.getAnnouncerFacade());
        this.requestEvent = event.getEvent();
    }
}
