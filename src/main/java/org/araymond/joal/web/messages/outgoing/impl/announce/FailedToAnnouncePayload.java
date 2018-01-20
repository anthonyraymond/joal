package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public class FailedToAnnouncePayload implements MessagePayload {
    private final InfoHash infoHash;
    private final String errMessage;
    private final int interval;

    public FailedToAnnouncePayload(final FailedToAnnounceEvent event) {
        this.infoHash = event.getInfoHash();
        this.errMessage = event.getErrMessage();
        this.interval = event.getInterval();
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public int getInterval() {
        return interval;
    }
}
