package org.araymond.joal.core.client.emulated.generator.key.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.RandomStringUtils;
import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;

/**
 * Created by raymo on 19/07/2017.
 */
public enum KeyTypes {
    @JsonProperty("hash")
    HASH,
    @JsonProperty("hash_no_leading_zero")
    HASH_NO_LEADING_ZERO;

    public String generateHash(final int length) {
        final String value;
        switch (this) {
            case HASH:
                value = createGenericHash(length);
                break;
            case HASH_NO_LEADING_ZERO:
                value = removeLeadingZeroes(createGenericHash(length));
                break;
            default:
                throw new TorrentClientConfigIntegrityException("Unhandled type: " + this.name());
        }
        return value;
    }

    private String createGenericHash(final int length) {
        return RandomStringUtils.random(length, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F');
    }

    @VisibleForTesting
    String removeLeadingZeroes(final String string) {
        return string.replaceFirst("^0+(?!$)", "");
    }
}
