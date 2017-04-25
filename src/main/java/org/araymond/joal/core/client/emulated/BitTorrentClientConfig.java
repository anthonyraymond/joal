package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final PeerIdInfo peerIdInfo;
    private final String query;
    private final KeyInfo keyInfo;
    private final List<HttpHeader> requestHeaders;
    private final Integer numwant;


    @JsonCreator
    TorrentClientConfig(
            @JsonProperty(value = "peerIdInfo", required = true) final PeerIdInfo peerIdInfo,
            @JsonProperty(value = "query", required = true) final String query,
            @JsonProperty("keyInfo") final KeyInfo keyInfo,
            @JsonProperty(value = "requestHeaders", required = true) final List<HttpHeader> requestHeaders,
            @JsonProperty(value = "numwant", required = true) final Integer numwant
    ) {
        this.peerIdInfo = peerIdInfo;
        this.query = query;
        this.keyInfo = keyInfo; // May be null
        this.requestHeaders = requestHeaders; // May be empty, but not null
        this.numwant = numwant;
    }

    BitTorrentClient createClient() {
        return new BitTorrentClient(
                this.peerIdInfo.generateNewPeerId(),
                this.generateNewKey().orElse(""),
                query,
                ImmutableList.copyOf(requestHeaders),
                numwant
        );
    }

    private Optional<String> generateNewKey() {
        if (Objects.isNull(this.keyInfo)) {
            return Optional.empty();
        }
        String key = this.keyInfo.getType().generateString(this.keyInfo.length);
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

    private static class PeerIdInfo {
        static int PEER_ID_LENGTH = 20;
        private final String prefix;
        private final ValueType type;
        private final boolean upperCase;
        private final boolean lowerCase;

        @JsonCreator
        public PeerIdInfo(
                @JsonProperty(value = "prefix", required = true) final String prefix,
                @JsonProperty(value = "type", required = true) final ValueType type,
                @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
                @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
        ) {
            this.prefix = prefix;
            this.type = type;
            this.upperCase = upperCase;
            this.lowerCase = lowerCase;
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

        String generateNewPeerId() {
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

        void validate() {
            if (StringUtils.isBlank(this.prefix)) {
                throw new IllegalArgumentException("Peer id cannot be null or empty.");
            }
            Preconditions.checkNotNull(this.type, "Peed id type cannot be null");
        }
    }

    private static class KeyInfo {
        private final Integer length;
        private final ValueType type;
        private final boolean upperCase;
        private final boolean lowerCase;

        public KeyInfo(
                @JsonProperty(value = "length", required = true) final Integer length,
                @JsonProperty(value = "type", required = true) final ValueType type,
                @JsonProperty(value = "upperCase", required = true) final boolean upperCase,
                @JsonProperty(value = "lowerCase", required = true) final boolean lowerCase
        ) {
            this.length = length;
            this.type = type;
            this.upperCase = upperCase;
            this.lowerCase = lowerCase;
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

        @JsonCreator
        HttpHeader(@JsonProperty(value = "name", required = true) final String name, @JsonProperty(value = "value", required = true) final String value) {
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
        @JsonProperty("alphabetic")
        ALPHABETIC,
        @JsonProperty("numeric")
        NUMERIC,
        @JsonProperty("alphanumeric")
        ALPHANUMERIC,
        @JsonProperty("random")
        RANDOM,
        @JsonProperty("printable")
        PRINTABLE;

        public String generateString(final int length) {
            final String value;
            switch (this) {
                case ALPHABETIC:
                    value = RandomStringUtils.randomAlphabetic(length);
                    break;
                case NUMERIC:
                    value = RandomStringUtils.randomNumeric(length);
                    break;
                case ALPHANUMERIC:
                    value = RandomStringUtils.randomAlphanumeric(length);
                    break;
                case RANDOM:
                    value = RandomStringUtils.random(length, IntStream.range(0, 256).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining()).toCharArray());
                    break;
                case PRINTABLE:
                    value = RandomStringUtils.randomPrint(length);
                    break;
                default:
                    throw new TorrentClientConfigIntegrityException("Unhandled type: " + this.name());
            }
            return value;
        }
    }

}
