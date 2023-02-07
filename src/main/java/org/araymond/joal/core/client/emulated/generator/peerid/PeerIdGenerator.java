package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.InfoHash;

/**
 * Created by raymo on 16/07/2017.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="refreshOn")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NeverRefreshPeerIdGenerator.class, name = "NEVER"),
        @JsonSubTypes.Type(value = AlwaysRefreshPeerIdGenerator.class, name = "ALWAYS"),
        @JsonSubTypes.Type(value = TimedRefreshPeerIdGenerator.class, name = "TIMED"),
        @JsonSubTypes.Type(value = TorrentVolatileRefreshPeerIdGenerator.class, name = "TORRENT_VOLATILE"),
        @JsonSubTypes.Type(value = TorrentPersistentRefreshPeerIdGenerator.class, name = "TORRENT_PERSISTENT")
})
@EqualsAndHashCode
@Getter
public abstract class PeerIdGenerator {
    public static final int PEER_ID_LENGTH = 20;

    @JsonProperty("algorithm")
    private final PeerIdAlgorithm algorithm;
    @JsonProperty("shouldUrlEncode")
    private final boolean shouldUrlEncode;

    protected PeerIdGenerator(final PeerIdAlgorithm algorithm, final boolean shouldUrlEncode) {
        if (algorithm == null) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm must not be null");
        }
        this.algorithm = algorithm;
        this.shouldUrlEncode = shouldUrlEncode;
    }

    @JsonIgnore
    public abstract String getPeerId(final InfoHash infoHash, RequestEvent event);

    protected String generatePeerId() {
        final String peerId = this.algorithm.generate();
        if (peerId.length() != PEER_ID_LENGTH) {
            throw new IllegalStateException("PeerId length was supposed to be " + PEER_ID_LENGTH + ", but a length of "
                    + peerId.length() + " was generated. Throw exception to prevent sending invalid PeerId to tracker");
        }
        return peerId;
    }
}
