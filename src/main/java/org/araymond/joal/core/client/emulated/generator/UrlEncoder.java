package org.araymond.joal.core.client.emulated.generator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.araymond.joal.core.client.emulated.utils.Casing;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.String.valueOf;

@EqualsAndHashCode(of = {"encodingExclusionPattern", "encodedHexCase"})
@Getter
public class UrlEncoder {

    private final String encodingExclusionPattern;
    private final Casing encodedHexCase;
    @JsonIgnore
    private final Pattern pattern;

    @JsonCreator
    public UrlEncoder(
            @JsonProperty(value = "encodingExclusionPattern", required = true) final String encodingExclusionPattern,
            @JsonProperty(value = "encodedHexCase", required = true) final Casing encodedHexCase
    ) {
        this.encodingExclusionPattern = encodingExclusionPattern;
        this.encodedHexCase = encodedHexCase;
        this.pattern = Pattern.compile(this.encodingExclusionPattern);
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
        if (pattern.matcher(valueOf(character)).matches()) {
            return valueOf(character);
        }

        final String hex = character == 0 ? "%00" : format("%%%02x", (int) character);
        return encodedHexCase.toCase(hex);
    }
}
