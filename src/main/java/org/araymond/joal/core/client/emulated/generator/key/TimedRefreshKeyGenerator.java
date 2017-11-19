package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshKeyGenerator extends KeyGenerator {

    private LocalDateTime lastGeneration;
    private String key;
    private final Integer refreshEvery;

    @JsonCreator
    TimedRefreshKeyGenerator(
            @JsonProperty(value = "refreshEvery", required = true) final Integer refreshEvery,
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        if (refreshEvery == null || refreshEvery < 1) {
            throw new TorrentClientConfigIntegrityException("refreshEvery must be greater than 0.");
        }
        this.refreshEvery = refreshEvery;
    }

    @JsonProperty("refreshEvery")
    Integer getRefreshEvery() {
        return refreshEvery;
    }

    @Override
    public String getKey(final MockedTorrent torrent, final RequestEvent event) {
        if (this.shouldRegenerateKey()) {
            this.lastGeneration = LocalDateTime.now();
            this.key = super.generateKey();
        }

        return this.key;
    }

    private boolean shouldRegenerateKey() {
        return this.lastGeneration == null || ChronoUnit.SECONDS.between(this.lastGeneration, LocalDateTime.now()) >= this.refreshEvery;
    }
}
