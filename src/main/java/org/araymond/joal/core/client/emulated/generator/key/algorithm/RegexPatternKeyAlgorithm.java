package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mifmif.common.regex.Generex;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

@EqualsAndHashCode(of = "pattern")
public class RegexPatternKeyAlgorithm implements KeyAlgorithm {

    @Getter
    @JsonProperty("pattern")
    private final String pattern;
    private final Generex generex;

    public RegexPatternKeyAlgorithm(
            @JsonProperty(value = "pattern", required = true) final String pattern
    ) {
        if (StringUtils.isBlank(pattern)) {
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
