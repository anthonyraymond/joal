package org.araymond.joal.client.emulated;

import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.araymond.joal.client.emulated.TorrentClientConfig.ValueType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by raymo on 15/04/2017.
 */
public class ValueTypeTest {

    @Test
    public void shouldSerializeAlphabetic() {
        assertThat(new Gson().toJson(ALPHABETIC)).isEqualTo("\"alphabetic\"");
    }
    
    @Test
    public void shouldDeserializeAlphabetic() {
        final TorrentClientConfig.ValueType type = new Gson().fromJson("alphabetic", TorrentClientConfig.ValueType.class);
        assertThat(type).isEqualTo(ALPHABETIC);
    }

    @Test
    public void shouldCreateAlphabeticString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(ALPHABETIC.generateString(100)).matches(Pattern.compile("\\p{Alpha}+"));
        }
    }

    @Test
    public void shouldSerializeNumeric() {
        assertThat(new Gson().toJson(NUMERIC)).isEqualTo("\"numeric\"");
    }

    @Test
    public void shouldDeserializeNumeric() {
        final TorrentClientConfig.ValueType type = new Gson().fromJson("numeric", TorrentClientConfig.ValueType.class);
        assertThat(type).isEqualTo(NUMERIC);
    }

    @Test
    public void shouldCreateNumericString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(NUMERIC.generateString(100)).matches(Pattern.compile("\\p{Digit}+"));
        }
    }

    @Test
    public void shouldSerializeAlphanumeric() {
        assertThat(new Gson().toJson(ALPHANUMERIC)).isEqualTo("\"alphanumeric\"");
    }

    @Test
    public void shouldDeserializeAlphanumeric() {
        final TorrentClientConfig.ValueType type = new Gson().fromJson("alphanumeric", TorrentClientConfig.ValueType.class);
        assertThat(type).isEqualTo(ALPHANUMERIC);
    }

    @Test
    public void shouldCreateAlphanumericString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(ALPHANUMERIC.generateString(100)).matches(Pattern.compile("\\p{Alnum}+"));
        }
    }

    @Test
    public void shouldSerializeRandom() {
        assertThat(new Gson().toJson(RANDOM)).isEqualTo("\"random\"");
    }

    @Test
    public void shouldDeserializeRandom() {
        final TorrentClientConfig.ValueType type = new Gson().fromJson("random", TorrentClientConfig.ValueType.class);
        assertThat(type).isEqualTo(RANDOM);
    }

    // Useless test
    @Test
    public void shouldCreateRandomString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(ALPHABETIC.generateString(100)).matches(Pattern.compile(".*"));
        }
    }

    @Test
    public void shouldSerializePrintable() {
        assertThat(new Gson().toJson(PRINTABLE)).isEqualTo("\"printable\"");
    }

    @Test
    public void shouldDeserializePrintable() {
        final TorrentClientConfig.ValueType type = new Gson().fromJson("printable", TorrentClientConfig.ValueType.class);
        assertThat(type).isEqualTo(PRINTABLE);
    }

    @Test
    public void shouldCreatePrintableString() {
        for (int i = 0; i < 30; ++i) {
            assertThat(PRINTABLE.generateString(100)).matches(Pattern.compile("\\p{Print}+"));
        }
    }

    @Ignore //TODO : make sure this test pass after moving from Gson to jackson
    @Test
    public void shouldFailForNonExistingType() {
        try {
            final TorrentClientConfig.ValueType type = new Gson().fromJson("woops", TorrentClientConfig.ValueType.class);
            type.generateString(8);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).contains("Unrecognized type");
        }
    }

}
