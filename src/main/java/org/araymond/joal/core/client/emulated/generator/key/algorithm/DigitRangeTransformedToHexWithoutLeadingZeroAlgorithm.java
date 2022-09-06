package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

import java.util.concurrent.ThreadLocalRandom;

@EqualsAndHashCode
public class DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm implements KeyAlgorithm {

    private final Long inclusiveLowerBound;
    private final Long inclusiveUpperBound;

    public DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(
            @JsonProperty(value = "inclusiveLowerBound", required = true) final Long inclusiveLowerBound,
            @JsonProperty(value = "inclusiveUpperBound", required = true) final Long inclusiveUpperBound
    ) {
        if (inclusiveLowerBound == null) {
            throw new TorrentClientConfigIntegrityException("inclusiveLowerBound algorithm length must not be null.");
        }
        if (inclusiveUpperBound == null) {
            throw new TorrentClientConfigIntegrityException("inclusiveUpperBound algorithm length must not be null.");
        }
        if (inclusiveUpperBound < inclusiveLowerBound) {
            throw new TorrentClientConfigIntegrityException("inclusiveUpperBound must be greater than inclusiveLowerBound.");
        }

        this.inclusiveLowerBound = inclusiveLowerBound;
        this.inclusiveUpperBound = inclusiveUpperBound;
    }

    long getRandomDigitBetween(final Long minInclusive, final Long maxInclusive) {
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1);
    }

    @Override
    public String generate() {
        final long randomDigit = this.getRandomDigitBetween(this.inclusiveLowerBound, this.inclusiveUpperBound);
        return Long.toHexString(randomDigit);
    }
}
