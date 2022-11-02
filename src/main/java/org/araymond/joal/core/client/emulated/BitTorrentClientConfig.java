package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;

import java.util.List;

/**
 * Created by raymo on 24/01/2017.
 */
@EqualsAndHashCode
@Getter
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

        if (this.query.contains("{key}") && this.keyGenerator == null) {
            throw new TorrentClientConfigIntegrityException("Query string contains {key}, but no keyGenerator was found in .client file.");
        }
    }

    @EqualsAndHashCode
    @Getter
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
    }
}
