package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasStoppedPayload extends AnnouncePayload {

    public AnnouncerHasStoppedPayload(final TorrentWithStats torrent) {
        super(torrent);
    }
}
