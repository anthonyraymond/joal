package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

import java.time.LocalDateTime;

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
