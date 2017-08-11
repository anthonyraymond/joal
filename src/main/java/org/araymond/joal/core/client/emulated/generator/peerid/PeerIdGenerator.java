package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mifmif.common.regex.Generex;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.araymond.joal.core.ttorent.client.MockedTorrent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final boolean isUrlEncoded;
    @JsonIgnore
    private final Generex generex;

    protected PeerIdGenerator(final String prefix, final String pattern, final boolean isUrlEncoded) {
        if (StringUtils.isBlank(prefix)) {
            throw new TorrentClientConfigIntegrityException("prefix must not be null or empty.");
        }
        if (StringUtils.isBlank(pattern)) {
            throw new TorrentClientConfigIntegrityException("peerId pattern must not be null or empty.");
        }
        this.prefix = prefix;
        this.pattern = pattern;
        this.isUrlEncoded = isUrlEncoded;
        this.generex = new Generex(pattern);
    }

    @JsonProperty("prefix")
    String getPrefix() {
        return prefix;
    }

    @JsonProperty("pattern")
    String getPattern() {
        return pattern;
    }

    @JsonProperty("isUrlEncoded")
    boolean getIsUrlEncoded() {
        return isUrlEncoded;
    }

    @JsonIgnore
    public abstract String getPeerId(final MockedTorrent torrent, RequestEvent event);

    protected String generatePeerId() {
        final String peerIdPrefix = this.getPrefix();
        String peerIdSuffix = this.generex.random();
        if (this.isUrlEncoded) {
            peerIdSuffix = this.urlEncodeLowerCasedSpecialChars(peerIdSuffix);
        }

        return peerIdPrefix + peerIdSuffix;
    }

    /**
     * UrlEncode the peerID, it does NOT change the casing of the regular characters, but it lower all encoded characters
     * @param peerId peerId to encode
     * @return encoded peerId
     */
    String urlEncodeLowerCasedSpecialChars(final String peerId) {
        final String urlEncodedPeerId;
        try {
            urlEncodedPeerId = URLEncoder.encode(peerId, Torrent.BYTE_ENCODING);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to URL encode the peerid.");
        }
        final Matcher m = Pattern.compile("%[A-Z-a-z0-9]{2}").matcher(urlEncodedPeerId);

        final StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(urlEncodedPeerId.substring(last, m.start()));
            sb.append(m.group(0).toLowerCase());
            last = m.end();
        }
        sb.append(urlEncodedPeerId.substring(last));

        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PeerIdGenerator peerIdGenerator = (PeerIdGenerator) o;
        return isUrlEncoded == peerIdGenerator.isUrlEncoded &&
                com.google.common.base.Objects.equal(prefix, peerIdGenerator.prefix) &&
                com.google.common.base.Objects.equal(pattern, peerIdGenerator.pattern);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(prefix, pattern, isUrlEncoded);
    }

}
