package org.araymond.joal.core.events;

import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.springframework.context.ApplicationEvent;

/**
 * Created by raymo on 06/05/2017.
 */
public class TorrentFileAddedForSeed {
    private final MockedTorrent torrent;

    public TorrentFileAddedForSeed(final MockedTorrent torrent) {
        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
