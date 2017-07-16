package org.araymond.joal.core.client.emulated.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.RandomStringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by raymo on 16/07/2017.
 */
public enum StringTypes {
    @JsonProperty("alphabetic")
    ALPHABETIC,
    @JsonProperty("numeric")
    NUMERIC,
    @JsonProperty("alphanumeric")
    ALPHANUMERIC,
    @JsonProperty("random")
    RANDOM,
    @JsonProperty("printable")
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
                // FROM 1 instead of 0, because i think i remember 0 should not be included.
                value = RandomStringUtils.random(length, IntStream.range(1, 255).mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining()).toCharArray());
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
