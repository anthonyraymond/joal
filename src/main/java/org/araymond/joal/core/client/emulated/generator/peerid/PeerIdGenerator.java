package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.client.emulated.generator.peerid.type.PeerIdTypes;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

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
    static final int PEER_ID_LENGTH = 20;
    private final String prefix;
    private final PeerIdTypes type;
    private final boolean upperCase;
    private final boolean lowerCase;

    protected PeerIdGenerator(final String prefix, final PeerIdTypes type, final boolean upperCase, final boolean lowerCase) {
        if (StringUtils.isBlank(prefix)) {
            throw new TorrentClientConfigIntegrityException("prefix must not be null or empty.");
        }
        if (type == null) {
            throw new TorrentClientConfigIntegrityException("peerId type must not be null.");
        }
        this.prefix = prefix;
        this.type = type;
        this.upperCase = upperCase;
        this.lowerCase = lowerCase;
    }

    @JsonProperty("prefix")
    String getPrefix() {
        return prefix;
    }

    @JsonProperty("type")
    PeerIdTypes getType() {
        return type;
    }

    @JsonProperty("upperCase")
    boolean isUpperCase() {
        return upperCase;
    }

    @JsonProperty("lowerCase")
    boolean isLowerCase() {
        return lowerCase;
    }

    @JsonIgnore
    public abstract String getPeerId(final MockedTorrent torrent, RequestEvent event);

    protected String generatePeerId() {
        final String peerIdPrefix = this.getPrefix();
        final int peerSuffixLength = PEER_ID_LENGTH - this.getPrefix().length();
        String peerIdSuffix = this.getType().generateString(peerSuffixLength);
        if (this.isUpperCase()) {
            peerIdSuffix = peerIdSuffix.toUpperCase();
        } else if (this.isLowerCase()) {
            peerIdSuffix = peerIdSuffix.toLowerCase();
        }

        return peerIdPrefix + peerIdSuffix;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PeerIdGenerator peerIdGenerator = (PeerIdGenerator) o;
        return upperCase == peerIdGenerator.upperCase &&
                lowerCase == peerIdGenerator.lowerCase &&
                com.google.common.base.Objects.equal(prefix, peerIdGenerator.prefix) &&
                type == peerIdGenerator.type;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(prefix, type, upperCase, lowerCase);
    }

}
