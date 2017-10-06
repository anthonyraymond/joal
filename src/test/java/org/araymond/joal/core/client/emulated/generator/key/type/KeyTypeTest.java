package org.araymond.joal.core.client.emulated.generator.key.type;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes.HASH;
import static org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes.HASH_NO_LEADING_ZERO;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 15/04/2017.
 */
public class KeyTypeTest {

    @Test
    public void shouldCreateSimpleHash() {
        for (int i = 0; i < 30; ++i) {
            assertThat(HASH.generateHash(100))
                    .hasSize(100)
                    .matches(Pattern.compile("[0-9A-Z]+"));
        }
    }

    // TODO : find a way to improve this test, we are not sure that the hash will have a heading 0,
    @Test
    public void shouldCreateHashWithoutTrailingZeroes() {
        for (int i = 0; i < 300; ++i) {
            assertThat(HASH_NO_LEADING_ZERO.generateHash(100))
                    .matches(Pattern.compile("[0-9A-Z]+"));
        }
    }

    @Test
    public void shouldRemoveHeadingZeroes() {
        assertThat(KeyTypes.HASH.removeLeadingZeroes("0AAAAAA")).isEqualTo("AAAAAA");
        assertThat(KeyTypes.HASH.removeLeadingZeroes("AFDE")).isEqualTo("AFDE");
        assertThat(KeyTypes.HASH.removeLeadingZeroes("0")).isEqualTo("0");
    }
}
