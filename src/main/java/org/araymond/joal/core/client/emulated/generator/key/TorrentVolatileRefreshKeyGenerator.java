package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshKeyGenerator extends KeyGenerator {
    private final Map<MockedTorrent, String> keyPerTorrent;

    @JsonCreator
    TorrentVolatileRefreshKeyGenerator(
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        keyPerTorrent = new HashMap<>();
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        final String key;

        if (!this.keyPerTorrent.containsKey(torrent)) {
            this.keyPerTorrent.put(torrent, super.generateKey());
        }

        key = this.keyPerTorrent.get(torrent);

        if (event == RequestEvent.STOPPED) {
            this.keyPerTorrent.remove(torrent);
        }

        return key;
    }
}
