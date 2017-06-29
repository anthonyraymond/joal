package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 26/06/2017.
 */
public abstract class AnnouncePayload implements MessagePayload {
    private final String id;
    private final String name;
    private final Long size;
    private final Long currentSpeed;

    protected AnnouncePayload(final TorrentWithStats torrent) {
        Preconditions.checkNotNull(torrent, "Torrent must not be null.");

        this.id = torrent.getTorrent().getHexInfoHash();
        this.name = torrent.getTorrent().getName();
        this.size = torrent.getTorrent().getSize();
        this.currentSpeed = torrent.getCurrentRandomSpeedInBytes();
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

    public Long getCurrentSpeed() {
        return currentSpeed;
    }
}
