package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public class FailedToAnnouncePayload extends AnnouncePayload {
    private final String errMessage;
    private final int interval;

    public FailedToAnnouncePayload(final FailedToAnnounceEvent event) {
        super(event.getInfoHash());
        this.errMessage = event.getErrMessage();
        this.interval = event.getInterval();
    }


    public String getErrMessage() {
        return errMessage;
    }

    public int getInterval() {
        return interval;
    }
}
