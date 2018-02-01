package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

public abstract class AnnouncePayload implements MessagePayload {
    private final InfoHash infoHash;

    protected AnnouncePayload(final InfoHash infoHash) {
        this.infoHash = infoHash;
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }
}
