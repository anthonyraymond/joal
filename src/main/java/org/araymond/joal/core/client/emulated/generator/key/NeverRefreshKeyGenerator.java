package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;

/**
 * Created by raymo on 16/07/2017.
 */
public class NeverRefreshKeyGenerator extends KeyGenerator {

    private final String key;

    @JsonCreator
    NeverRefreshKeyGenerator(
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        this.key = generateKey();
    }

    @Override
    public String getKey(final InfoHash infoHash, final RequestEvent event) {
        return key;
    }
}
