package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mifmif.common.regex.Generex;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
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
    public static final int PEER_ID_LENGTH = 20;
    private final String prefix;
    private final String pattern;
    @JsonIgnore
    private final Generex generex;

    protected PeerIdGenerator(final String prefix, final String pattern) {
        if (StringUtils.isBlank(prefix)) {
            throw new TorrentClientConfigIntegrityException("prefix must not be null or empty.");
        }
        if (StringUtils.isBlank(pattern)) {
            throw new TorrentClientConfigIntegrityException("peerId pattern must not be null or empty.");
        }
        this.prefix = prefix;
        this.pattern = pattern;
        // FIXME : remove the size {...} trick as soon as https://github.com/mifmif/Generex/issues/42 is fixed
        this.generex = new Generex(pattern + "{" + (PEER_ID_LENGTH - prefix.length()) + "}");
    }

    @JsonProperty("prefix")
    String getPrefix() {
        return prefix;
    }

    @JsonProperty("pattern")
    String getPattern() {
        return pattern;
    }

    @JsonIgnore
    public abstract String getPeerId(final MockedTorrent torrent, RequestEvent event);

    protected String generatePeerId() {
        final String peerIdPrefix = this.getPrefix();
        final int peerSuffixLength = PEER_ID_LENGTH - this.getPrefix().length();
        final String peerIdSuffix = this.generex.random(peerSuffixLength, peerSuffixLength);

        return peerIdPrefix + peerIdSuffix;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PeerIdGenerator peerIdGenerator = (PeerIdGenerator) o;
        return com.google.common.base.Objects.equal(prefix, peerIdGenerator.prefix) &&
                com.google.common.base.Objects.equal(pattern, peerIdGenerator.pattern);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(prefix, pattern);
    }

}
