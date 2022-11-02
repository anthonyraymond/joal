package org.araymond.joal.web.messages.outgoing.impl.announce;

import lombok.Getter;
import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;

@Getter
public class FailedToAnnouncePayload extends AnnouncePayload {
    private final String errMessage;

    public FailedToAnnouncePayload(final FailedToAnnounceEvent event) {
        super(event.getAnnouncerFacade());
        this.errMessage = event.getErrMessage();
    }
}
