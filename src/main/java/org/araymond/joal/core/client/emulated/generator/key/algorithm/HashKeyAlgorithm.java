package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

@EqualsAndHashCode
@Getter
public class HashKeyAlgorithm implements KeyAlgorithm {

    @JsonProperty("length")
    private final Integer length;

    public HashKeyAlgorithm(
            @JsonProperty(value = "length", required = true) final Integer length
    ) {
        if (length == null) {
            throw new TorrentClientConfigIntegrityException("key algorithm length must not be null.");
        }

        this.length = length;
    }

    @Override
    public String generate() {
        return RandomStringUtils.random(this.length, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');
    }
}
