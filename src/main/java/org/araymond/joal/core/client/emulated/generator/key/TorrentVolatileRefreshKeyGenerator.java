package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by raymo on 16/07/2017.
 */
public class TorrentVolatileRefreshKeyGenerator extends KeyGenerator {
    private final Map<InfoHash, String> keyPerTorrent;

    @JsonCreator
    TorrentVolatileRefreshKeyGenerator(
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        keyPerTorrent = new ConcurrentHashMap<>();
    }

    @Override
    public String getKey(final InfoHash infoHash, final RequestEvent event) {
        final String key;

        if (!this.keyPerTorrent.containsKey(infoHash)) {
            this.keyPerTorrent.put(infoHash, super.generateKey());
        }

        key = this.keyPerTorrent.get(infoHash);

        if (event == RequestEvent.STOPPED) {
            this.keyPerTorrent.remove(infoHash);
        }

        return key;
    }
}
