package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by raymo on 24/01/2017.
 */
class BitTorrentClientConfig {
    @JsonProperty("peerIdInfo")
    private final PeerIdInfo peerIdInfo;
    @JsonProperty("query")
    private final String query;
    @JsonProperty("keyInfo")
    private final KeyInfo keyInfo;
    @JsonProperty("requestHeaders")
    private final List<HttpHeader> requestHeaders;
    @JsonProperty("numwant")
    private final Integer numwant;

    @JsonCreator
    BitTorrentClientConfig(
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

        if (this.query.contains("{key}")) {
            if (this.keyInfo == null) {
                throw new TorrentClientConfigIntegrityException("Query string contains {key}, but no keyInfo was found in .client file.");
            }
        }
    }

    BitTorrentClient createClient() {
        return new BitTorrentClient(
                this.peerIdInfo.generateNewPeerId(),
                this.generateNewKey().orElse(null),
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BitTorrentClientConfig that = (BitTorrentClientConfig) o;
        return com.google.common.base.Objects.equal(peerIdInfo, that.peerIdInfo) &&
                com.google.common.base.Objects.equal(query, that.query) &&
                com.google.common.base.Objects.equal(keyInfo, that.keyInfo) &&
                com.google.common.base.Objects.equal(requestHeaders, that.requestHeaders) &&
                com.google.common.base.Objects.equal(numwant, that.numwant);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(peerIdInfo, query, keyInfo, requestHeaders, numwant);
    }

    static class PeerIdInfo {
        static final int PEER_ID_LENGTH = 20;
        private final String prefix;
        private final ValueType type;
        private final boolean upperCase;
        private final boolean lowerCase;

        @JsonCreator
        PeerIdInfo(
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

        @JsonProperty("prefix")
        String getPrefix() {
            return prefix;
        }

        @JsonProperty("type")
        ValueType getType() {
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

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PeerIdInfo that = (PeerIdInfo) o;
            return upperCase == that.upperCase &&
                    lowerCase == that.lowerCase &&
                    com.google.common.base.Objects.equal(prefix, that.prefix) &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(prefix, type, upperCase, lowerCase);
        }
    }

    static class KeyInfo {
        private final Integer length;
        private final ValueType type;
        private final boolean upperCase;
        private final boolean lowerCase;

        KeyInfo(
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


        @JsonProperty("length")
        public int getLength() {
            return length;
        }

        @JsonProperty("type")
        public ValueType getType() {
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

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final KeyInfo keyInfo = (KeyInfo) o;
            return upperCase == keyInfo.upperCase &&
                    lowerCase == keyInfo.lowerCase &&
                    com.google.common.base.Objects.equal(length, keyInfo.length) &&
                    type == keyInfo.type;
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(length, type, upperCase, lowerCase);
        }
    }

    public static class HttpHeader {
        private final String name;
        private final String value;

        @JsonCreator
        HttpHeader(@JsonProperty(value = "name", required = true) final String name, @JsonProperty(value = "value", required = true) final String value) {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(value);
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final HttpHeader header = (HttpHeader) o;
            return com.google.common.base.Objects.equal(name, header.name) &&
                    com.google.common.base.Objects.equal(value, header.value);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(name, value);
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
                    // FROM 1 instead of 0, because i think i remember 0 should not be included.
                    value = RandomStringUtils.random(length, IntStream.range(1, 255).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining()).toCharArray());
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
