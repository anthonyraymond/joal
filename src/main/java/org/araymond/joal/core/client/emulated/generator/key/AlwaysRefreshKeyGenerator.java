package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

/**
 * Created by raymo on 16/07/2017.
 */
public class AlwaysRefreshKeyGenerator extends KeyGenerator {

    @JsonCreator
    AlwaysRefreshKeyGenerator(
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        return super.generateKey();
    }

}
