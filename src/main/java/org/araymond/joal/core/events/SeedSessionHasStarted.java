package org.araymond.joal.core.events;

import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

/**
 * Created by raymo on 06/05/2017.
 */
public class SeedSessionHasStarted {

    private final BitTorrentClient bitTorrentClient;
    private final MockedTorrent torrent;

    public SeedSessionHasStarted(final BitTorrentClient bitTorrentClient, final MockedTorrent torrent) {
        this.bitTorrentClient = bitTorrentClient;
        this.torrent = torrent;
    }

    public BitTorrentClient getBitTorrentClient() {
        return bitTorrentClient;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

}
