package org.araymond.joal.core.client.emulated.generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import org.araymond.joal.core.client.emulated.utils.Casing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlEncoder {

    @JsonProperty("encodingExclusionPattern")
    private final String encodingExclusionPattern;
    @JsonProperty("encodedHexCase")
    private final Casing encodedHexCase;
    @JsonIgnore
    private final Pattern pattern;

    @JsonCreator
    public UrlEncoder(
            @JsonProperty(value = "encodingExclusionPattern" ,required = true) final String encodingExclusionPattern,
            @JsonProperty(value = "encodedHexCase" ,required = true) final Casing encodedHexCase
    ) {
        this.encodingExclusionPattern = encodingExclusionPattern;
        this.pattern = Pattern.compile(this.encodingExclusionPattern);
        this.encodedHexCase = encodedHexCase;
    }

    String getEncodingExclusionPattern() {
        return encodingExclusionPattern;
    }

    Casing getEncodedHexCase() {
        return encodedHexCase;
    }

    /**
     * UrlEncode a string, it does NOT change the casing of the regular characters, but it lower all encoded characters
     * @param toBeEncoded string to encode
     * @return encoded string
     */
    public String encode(final String toBeEncoded) {
        final StringBuilder sb = new StringBuilder();
        for (final char ch : toBeEncoded.toCharArray()) {
            sb.append(this.urlEncodeChar(ch));
        }
        return sb.toString();
    }

    @VisibleForTesting
    String urlEncodeChar(final char character) {
        final Matcher matcher = pattern.matcher("" + character);
        if (matcher.matches()) {
            return "" + character;
        }
        final String hex;
        if (character == 0) {
            hex = "%00";
        } else {
            hex = String.format("%%%02x", (int) character);
        }

        return encodedHexCase.toCase(hex);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UrlEncoder that = (UrlEncoder) o;
        return Objects.equal(encodingExclusionPattern, that.encodingExclusionPattern) &&
                encodedHexCase == that.encodedHexCase;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(encodingExclusionPattern, encodedHexCase);
    }

}
