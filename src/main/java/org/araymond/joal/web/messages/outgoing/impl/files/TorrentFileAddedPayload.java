package org.araymond.joal.web.messages.outgoing.impl.files;

import org.araymond.joal.core.events.filechange.TorrentFileAddedEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 10/07/2017.
 */
public class TorrentFileAddedPayload implements MessagePayload {
    private final String id;
    private final String name;
    private final Long size;

    public TorrentFileAddedPayload(final String id, final String name, final Long size) {
        this.id = id;
        this.name = name;
        this.size = size;
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
