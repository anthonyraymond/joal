package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.generation.PeerIdAlgorithm;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;

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
public abstract class PeerIdGenerator {
    public static final int PEER_ID_LENGTH = 20;
    private final PeerIdAlgorithm algorithm;
    private final boolean shouldUrlEncode;

    protected PeerIdGenerator(final PeerIdAlgorithm algorithm, final boolean shouldUrlEncode) {
        if (algorithm == null) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm must not be null.");
        }
        this.algorithm = algorithm;
        this.shouldUrlEncode = shouldUrlEncode;
    }

    @JsonProperty("algorithm")
    PeerIdAlgorithm getAlgorithm() {
        return algorithm;
    }

    @JsonProperty("shouldUrlEncode")
    public boolean getShouldUrlEncoded() {
        return shouldUrlEncode;
    }

    @JsonIgnore
    public abstract String getPeerId(final MockedTorrent torrent, RequestEvent event);

    protected String generatePeerId() {
        return this.algorithm.generate();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PeerIdGenerator peerIdGenerator = (PeerIdGenerator) o;
        return shouldUrlEncode == peerIdGenerator.shouldUrlEncode &&
                com.google.common.base.Objects.equal(algorithm, peerIdGenerator.algorithm);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(algorithm, shouldUrlEncode);
    }

}
