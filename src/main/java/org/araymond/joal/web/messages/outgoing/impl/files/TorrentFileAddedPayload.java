package org.araymond.joal.web.messages.outgoing.impl.files;

import org.araymond.joal.core.events.torrent.files.TorrentFileAddedEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 10/07/2017.
 */
public class TorrentFileAddedPayload implements MessagePayload {
    private final InfoHash infoHash;
    private final String name;
    private final Long size;

    public TorrentFileAddedPayload(final TorrentFileAddedEvent event) {
        this.infoHash = event.getTorrent().getTorrentInfoHash();
        this.name = event.getTorrent().getName();
        this.size = event.getTorrent().getSize();
    }

    public InfoHash getInfoHash() {
        return infoHash;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }
}
