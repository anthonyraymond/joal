package org.araymond.joal.core.client.emulated;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.ValueType.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 15/04/2017.
 */
public class ValueTypeTest {

    @Test
    public void shouldCreateAlphabeticString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(ALPHABETIC.generateString(100)).matches(Pattern.compile("\\p{Alpha}+"));
        }
    }

    @Test
    public void shouldCreateNumericString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(NUMERIC.generateString(100)).matches(Pattern.compile("\\p{Digit}+"));
        }
    }

    @Test
    public void shouldCreateAlphanumericString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(ALPHANUMERIC.generateString(100)).matches(Pattern.compile("\\p{Alnum}+"));
        }
    }

    // TODO : find a way to test this properly...
    @Test
    public void shouldCreateRandomString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(RANDOM.generateString(100)).hasSize(100);
        }
    }

    @Test
    public void shouldCreatePrintableString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(PRINTABLE.generateString(100)).matches(Pattern.compile("\\p{Print}+"));
        }
    }

}
