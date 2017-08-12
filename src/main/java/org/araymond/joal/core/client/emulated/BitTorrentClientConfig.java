package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;

import java.util.List;

/**
 * Created by raymo on 24/01/2017.
 */
public class BitTorrentClientConfig {
    @JsonProperty("peerIdGenerator")
    private final PeerIdGenerator peerIdGenerator;
    @JsonProperty("query")
    private final String query;
    @JsonProperty("keyGenerator")
    private final KeyGenerator keyGenerator;
    @JsonProperty("urlEncoder")
    private final UrlEncoder urlEncoder;
    @JsonProperty("requestHeaders")
    private final List<HttpHeader> requestHeaders;
    @JsonProperty("numwant")
    private final Integer numwant;
    @JsonProperty("numwantOnStop")
    private final Integer numwantOnStop;

    @JsonCreator
    BitTorrentClientConfig(
            @JsonProperty(value = "peerIdGenerator", required = true) final PeerIdGenerator peerIdGenerator,
            @JsonProperty(value = "query", required = true) final String query,
            @JsonProperty(value = "keyGenerator") final KeyGenerator keyGenerator,
            @JsonProperty(value = "urlEncoder", required = true) final UrlEncoder urlEncoder,
            @JsonProperty(value = "requestHeaders", required = true) final List<HttpHeader> requestHeaders,
            @JsonProperty(value = "numwant", required = true) final Integer numwant,
            @JsonProperty(value = "numwantOnStop", required = true) final Integer numwantOnStop
    ) {
        this.peerIdGenerator = peerIdGenerator;
        this.query = query;
        this.keyGenerator = keyGenerator; // May be null
        this.urlEncoder = urlEncoder;
        this.requestHeaders = requestHeaders; // May be empty, but not null
        this.numwant = numwant;
        this.numwantOnStop = numwantOnStop;

        if (this.query.contains("{key}")) {
            if (this.keyGenerator == null) {
                throw new TorrentClientConfigIntegrityException("Query string contains {key}, but no keyGenerator was found in .client file.");
            }
        }
    }

    @VisibleForTesting
    public BitTorrentClient createClient() {
        return new BitTorrentClient(
                this.peerIdGenerator,
                this.keyGenerator,
                this.urlEncoder,
                query,
                ImmutableList.copyOf(requestHeaders),
                new NumwantProvider(this.numwant, this.numwantOnStop)
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BitTorrentClientConfig that = (BitTorrentClientConfig) o;
        return com.google.common.base.Objects.equal(peerIdGenerator, that.peerIdGenerator) &&
                com.google.common.base.Objects.equal(urlEncoder, that.urlEncoder) &&
                com.google.common.base.Objects.equal(query, that.query) &&
                com.google.common.base.Objects.equal(keyGenerator, that.keyGenerator) &&
                com.google.common.base.Objects.equal(requestHeaders, that.requestHeaders) &&
                com.google.common.base.Objects.equal(numwant, that.numwant) &&
                com.google.common.base.Objects.equal(numwantOnStop, that.numwantOnStop);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(peerIdGenerator, query, keyGenerator, urlEncoder, requestHeaders, numwant, numwantOnStop);
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

}
