package org.araymond.joal.web.messages.outgoing.impl.files;

import org.araymond.joal.core.events.filechange.TorrentFileDeletedEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 10/07/2017.
 */
public class TorrentFileDeletedPayload implements MessagePayload {
    private final String id;
    private final String name;
    private final Long size;

    public TorrentFileDeletedPayload(final TorrentFileDeletedEvent event) {
        this.id = event.getTorrent().getHexInfoHash();
        this.name = event.getTorrent().getName();
        this.size = event.getTorrent().getSize();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }
}
