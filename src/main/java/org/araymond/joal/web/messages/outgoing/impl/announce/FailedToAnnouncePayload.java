package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;

public class FailedToAnnouncePayload extends AnnouncePayload {
    private final String errMessage;

    public FailedToAnnouncePayload(final FailedToAnnounceEvent event) {
        super(event.getAnnouncerFacade());
        this.errMessage = event.getErrMessage();
    }

    public String getErrMessage() {
        return errMessage;
    }
}
