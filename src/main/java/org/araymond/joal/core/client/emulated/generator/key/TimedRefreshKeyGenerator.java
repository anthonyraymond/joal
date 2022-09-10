package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshKeyGenerator extends KeyGenerator {
    @VisibleForTesting
    LocalDateTime lastGeneration;

    @Getter
    @JsonIgnore
    private String key;

    @Getter
    private final int refreshEvery;

    @JsonCreator
    TimedRefreshKeyGenerator(
            @JsonProperty(value = "refreshEvery", required = true) final int refreshEvery,
            @JsonProperty(value = "algorithm", required = true) final KeyAlgorithm algorithm,
            @JsonProperty(value = "keyCase", required = true) final Casing keyCase
    ) {
        super(algorithm, keyCase);
        if (refreshEvery < 1) {
            throw new TorrentClientConfigIntegrityException("refreshEvery must be greater than 0");
        }
        this.refreshEvery = refreshEvery;
    }

    @Override
    public String getKey(final InfoHash infoHash, final RequestEvent event) {
        if (this.shouldRegenerateKey()) {
            this.lastGeneration = LocalDateTime.now();
            this.key = super.generateKey();
        }

        return this.key;
    }

    boolean shouldRegenerateKey() {
        return this.lastGeneration == null || ChronoUnit.SECONDS.between(this.lastGeneration, LocalDateTime.now()) >= this.refreshEvery;
    }

    void setKey(String key) {
        this.key = key;
    }
}
