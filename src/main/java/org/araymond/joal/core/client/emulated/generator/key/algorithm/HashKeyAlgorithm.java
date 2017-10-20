package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

public class HashKeyAlgorithm implements KeyAlgorithm {

    private final Integer length;

    public HashKeyAlgorithm(
            @JsonProperty(value = "length", required = true) final Integer length
    ) {
        if (length == null) {
            throw new TorrentClientConfigIntegrityException("key algorithm length must not be null.");
        }

        this.length = length;
    }

    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    @Override
    public String generate() {
        return RandomStringUtils.random(this.length, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HashKeyAlgorithm that = (HashKeyAlgorithm) o;
        return Objects.equal(length, that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(length);
    }
}
