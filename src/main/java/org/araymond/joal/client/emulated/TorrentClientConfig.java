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

    EmulatedClient createClient() {
        return new EmulatedClient(
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
        @SerializedName("alphabetic")
        ALPHABETIC,
        @SerializedName("numeric")
        NUMERIC,
        @SerializedName("alphanumeric")
        ALPHANUMERIC,
        @SerializedName("random")
        RANDOM,
        @SerializedName("printable")
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
