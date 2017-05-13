package org.araymond.joal.core.events;

import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 05/05/2017.
 */
public class NoMoreLeechers {

    private final MockedTorrent torrent;

    public NoMoreLeechers(final MockedTorrent torrent) {
        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
