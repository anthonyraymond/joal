package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by raymo on 16/07/2017.
 */
public class TimedRefreshPeerIdGenerator extends PeerIdGenerator {

    @VisibleForTesting
    LocalDateTime lastGeneration;
    private String peerId;

    @JsonProperty("refreshEvery")
    @Getter
    private final Integer refreshEvery;

    @JsonCreator
    TimedRefreshPeerIdGenerator(
            @JsonProperty(value = "refreshEvery", required = true) final Integer refreshEvery,
            @JsonProperty(value = "algorithm", required = true) final PeerIdAlgorithm algorithm,
            @JsonProperty(value = "shouldUrlEncode", required = true) final boolean isUrlEncoded
    ) {
        super(algorithm, isUrlEncoded);
        if (refreshEvery == null || refreshEvery < 1) {
            throw new TorrentClientConfigIntegrityException("refreshEvery must be greater than 0.");
        }
        this.refreshEvery = refreshEvery;
    }

    @Override
    public String getPeerId(final InfoHash infoHash, final RequestEvent event) {
        if (this.shouldRegeneratePeerId()) {
            this.lastGeneration = LocalDateTime.now();
            this.peerId = super.generatePeerId();
        }

        return this.peerId;
    }

    private boolean shouldRegeneratePeerId() {
        return this.lastGeneration == null || ChronoUnit.SECONDS.between(this.lastGeneration, LocalDateTime.now()) >= this.refreshEvery;
    }
}
