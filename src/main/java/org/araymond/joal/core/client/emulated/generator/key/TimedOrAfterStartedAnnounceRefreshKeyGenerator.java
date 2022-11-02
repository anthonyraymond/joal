package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.time.LocalDateTime;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedOrAfterStartedAnnounceRefreshKeyGenerator extends TimedRefreshKeyGenerator {

    @JsonCreator
    TimedOrAfterStartedAnnounceRefreshKeyGenerator(
            @JsonProperty(value = "refreshEvery", required = true) final Integer refreshEvery,
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(refreshEvery, algorithm, keyCase);
    }

    @Override
    public String getKey(final InfoHash infoHash, final RequestEvent event) {
        if (super.shouldRegenerateKey()) {
            this.lastGeneration = LocalDateTime.now();
            setKey(generateKey());
        }

        final String key = getKey();

        if (event == RequestEvent.STARTED) {
            setKey(generateKey());
        }

        return key;
    }
}
