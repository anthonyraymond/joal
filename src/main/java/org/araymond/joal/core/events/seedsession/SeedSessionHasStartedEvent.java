package org.araymond.joal.core.events.seedsession;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.client.emulated.BitTorrentClient;

/**
 * Created by raymo on 06/05/2017.
 */
public class SeedSessionHasStartedEvent {

    private final BitTorrentClient bitTorrentClient;

    public SeedSessionHasStartedEvent(final BitTorrentClient bitTorrentClient) {
        Preconditions.checkNotNull(bitTorrentClient, "BitTorrentClient cannot be null");
        this.bitTorrentClient = bitTorrentClient;
    }

    public BitTorrentClient getBitTorrentClient() {
        return bitTorrentClient;
    }

}
