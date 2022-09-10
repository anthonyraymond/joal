package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

import java.util.concurrent.ThreadLocalRandom;

@EqualsAndHashCode
public class DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm implements KeyAlgorithm {

    private final long inclusiveLowerBound;
    private final long inclusiveUpperBound;

    public DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(
            @JsonProperty(value = "inclusiveLowerBound", required = true) final long inclusiveLowerBound,
            @JsonProperty(value = "inclusiveUpperBound", required = true) final long inclusiveUpperBound
    ) {
        if (inclusiveUpperBound < inclusiveLowerBound) {
            throw new TorrentClientConfigIntegrityException("inclusiveUpperBound must be greater than inclusiveLowerBound");
        }

        this.inclusiveLowerBound = inclusiveLowerBound;
        this.inclusiveUpperBound = inclusiveUpperBound;
    }

    long getRandomDigitBetween(final Long minInclusive, final long maxInclusive) {
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1);
    }

    @Override
    public String generate() {
        final long randomDigit = this.getRandomDigitBetween(this.inclusiveLowerBound, this.inclusiveUpperBound);
        return Long.toHexString(randomDigit);
    }
}
