package org.araymond.joal.client.emulated;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by raymo on 24/01/2017.
 */
class TorrentClientConfig {
    @SuppressWarnings("unused") private PeerIdInfo peerIdInfo; // JSON provided
    @SuppressWarnings("unused") private String query;  // JSON provided
    @SuppressWarnings("unused") private KeyInfo keyInfo;  // JSON provided
    private List<HttpHeader> requestHeaders = new ArrayList<>();
    private Integer numwant = null;

    TorrentClientConfig() {
    }

    public List<HttpHeader> getRequestHeaders() {
        return ImmutableList.copyOf(requestHeaders);
    }

    public String getQuery() {
        return query;
    }

    public Integer getNumwant() {
        return numwant;
    }

    String createNewPeerId() {
        final String peerIdPrefix = this.peerIdInfo.getPrefix();
        final int peerSuffixLength = PeerIdInfo.PEER_ID_LENGTH - this.peerIdInfo.getPrefix().length();
        String peerIdSuffix;
        switch (this.peerIdInfo.getType()) {
            case ALPHABETIC:
                peerIdSuffix = RandomStringUtils.randomAlphabetic(peerSuffixLength);
                break;
            case NUMERIC:
                peerIdSuffix = RandomStringUtils.randomNumeric(peerSuffixLength);
                break;
            case ALPHANUMERIC:
                peerIdSuffix = RandomStringUtils.randomAlphanumeric(peerSuffixLength);
                break;
            case RANDOM:
                peerIdSuffix = RandomStringUtils.random(peerSuffixLength, IntStream.range(0, 256).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining()).toCharArray());
                break;
            case PRINTABLE:
                peerIdSuffix = RandomStringUtils.randomPrint(peerSuffixLength);
                break;
            default:
                throw new TorrentClientConfigIntegrityException("Unhandled peer id type: " + this.peerIdInfo.getType());
        }
        if (this.peerIdInfo.isUpperCase()) {
            peerIdSuffix = peerIdSuffix.toUpperCase();
        } else if (this.peerIdInfo.isLowerCase()) {
            peerIdSuffix = peerIdSuffix.toLowerCase();
        }

        return peerIdPrefix + peerIdSuffix;
    }

    Optional<String> createNewKey() {
        if (Objects.isNull(this.keyInfo)) {
            return Optional.empty();
        }
        String key;
        switch (this.peerIdInfo.getType()) {
            case ALPHABETIC:
                key = RandomStringUtils.randomAlphabetic(this.keyInfo.length);
                break;
            case NUMERIC:
                key = RandomStringUtils.randomNumeric(this.keyInfo.length);
                break;
            case ALPHANUMERIC:
                key = RandomStringUtils.randomAlphanumeric(this.keyInfo.length);
                break;
            case RANDOM:
                key = RandomStringUtils.random(this.keyInfo.length, IntStream.range(0, 256).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining()).toCharArray());
                break;
            case PRINTABLE:
                key = RandomStringUtils.randomPrint(this.keyInfo.length);
                break;
            default:
                throw new TorrentClientConfigIntegrityException("Unhandled key type: " + this.peerIdInfo.getType());
        }
        if (this.keyInfo.isUpperCase()) {
            key = key.toUpperCase();
        } else if (this.keyInfo.isLowerCase()) {
            key = key.toLowerCase();
        }

        return Optional.of(key);
    }

    void validate() {
        if (this.peerIdInfo == null) {
            throw new TorrentClientConfigIntegrityException("peerIdInfo is required.");
        }
        this.peerIdInfo.validate();
        if (StringUtils.isBlank(this.query)) {
            throw new TorrentClientConfigIntegrityException("query is required.");
        }
        if (Objects.isNull(this.requestHeaders)) {
            throw new TorrentClientConfigIntegrityException("requestHeaders must not be null,(it may be empty).");
        }
        if (this.query.contains("{key}")) {
            if (this.keyInfo == null) {
                throw new TorrentClientConfigIntegrityException("Query string contains {key}, but no keyInfo was found in .client file.");
            }
            this.keyInfo.validate();
        }
    }

    static class PeerIdInfo {
        static int PEER_ID_LENGTH = 20;
        private String prefix;
        private ValueType type;
        private boolean upperCase = false;
        private boolean lowerCase = false;

        PeerIdInfo() {
        }

        String getPrefix() {
            return prefix;
        }

        ValueType getType() {
            return type;
        }

        boolean isUpperCase() {
            return upperCase;
        }

        boolean isLowerCase() {
            return lowerCase;
        }

        void validate() {
            if (StringUtils.isBlank(this.prefix)) {
                throw new IllegalArgumentException("Peer id cannot be null or empty.");
            }
            Preconditions.checkNotNull(this.type, "Peed id type cannot be null");
        }
    }

    static class KeyInfo {
        private Integer length;
        private ValueType type;
        private boolean upperCase = false;
        private boolean lowerCase = false;

        KeyInfo() {
        }

        public int getLength() {
            return length;
        }

        public ValueType getType() {
            return type;
        }

        boolean isUpperCase() {
            return upperCase;
        }

        boolean isLowerCase() {
            return lowerCase;
        }

        void validate() {
            Preconditions.checkNotNull(this.length, "Key length cannot be null");
            Preconditions.checkNotNull(this.type, "Key type cannot be null");
        }
    }

    public static class HttpHeader {
        private final String name;
        private final String value;

        HttpHeader(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    enum ValueType {
        @SerializedName("random")
        ALPHABETIC,
        @SerializedName("random")
        NUMERIC,
        @SerializedName("alphanumeric")
        ALPHANUMERIC,
        @SerializedName("random")
        RANDOM,
        @SerializedName("printable")
        PRINTABLE
    }

}
