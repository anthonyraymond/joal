package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mifmif.common.regex.Generex;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

@EqualsAndHashCode(of = "pattern")
public class RegexPatternPeerIdAlgorithm implements PeerIdAlgorithm {

    @JsonProperty("pattern")
    @Getter
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

    @Override
    public String generate() {
        return this.generex.random();
    }
}
