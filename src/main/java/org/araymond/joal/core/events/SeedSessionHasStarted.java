package org.araymond.joal.core.events;

import org.araymond.joal.core.client.emulated.BitTorrentClient;

/**
 * Created by raymo on 06/05/2017.
 */
public class SeedSessionHasStarted {

    private final BitTorrentClient bitTorrentClient;

    public SeedSessionHasStarted(final BitTorrentClient bitTorrentClient) {
        this.bitTorrentClient = bitTorrentClient;
    }

    public BitTorrentClient getBitTorrentClient() {
        return bitTorrentClient;
    }

}
