package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.mifmif.common.regex.Generex;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

public class RegexPatternPeerIdAlgorithm implements PeerIdAlgorithm {

    private final String pattern;
    private final Generex generex;

    public RegexPatternPeerIdAlgorithm(
            @JsonProperty(value = "pattern", required = true) final String pattern
    ) {
        if (pattern == null) {
            throw new TorrentClientConfigIntegrityException("peerId algorithm pattern must not be null.");
        }
        this.pattern = pattern;
        this.generex = new Generex(pattern);
    }

    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    @Override
    public String generate() {
        return this.generex.random();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RegexPatternPeerIdAlgorithm that = (RegexPatternPeerIdAlgorithm) o;
        return Objects.equal(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pattern);
    }
}
