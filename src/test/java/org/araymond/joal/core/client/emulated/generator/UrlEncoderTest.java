package org.araymond.joal.core.client.emulated.generator;

import org.araymond.joal.core.client.emulated.utils.Casing;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlEncoderTest {

    public static UrlEncoder createDefault() {
        return new UrlEncoder("[A-Za-z0-9]", Casing.LOWER);
    }

    @Test
    public void shouldEncodeChars() {
        final UrlEncoder urlEncoder = new UrlEncoder("", Casing.NONE);

        assertThat(urlEncoder.urlEncodeChar((char) 0x00)).isEqualToIgnoringCase("%00");
        assertThat(urlEncoder.urlEncodeChar((char) 0x01)).isEqualToIgnoringCase("%01");
        assertThat(urlEncoder.urlEncodeChar((char) 0x10)).isEqualToIgnoringCase("%10");
        assertThat(urlEncoder.urlEncodeChar((char) 0x1e)).isEqualToIgnoringCase("%1e");
        assertThat(urlEncoder.urlEncodeChar((char) 0x32)).isEqualToIgnoringCase("%32");
        assertThat(urlEncoder.urlEncodeChar((char) 0x7a)).isEqualToIgnoringCase("%7a");
        assertThat(urlEncoder.urlEncodeChar((char) 0xff)).isEqualToIgnoringCase("%ff");
    }

    @Test
    public void shouldNotEncodeCharsIfRegexIsDotStar() {
        final UrlEncoder urlEncoder = new UrlEncoder(".*", Casing.NONE);

        assertThat(urlEncoder.urlEncodeChar((char) 0x32)).isEqualToIgnoringCase("2");
        assertThat(urlEncoder.urlEncodeChar((char) 0x6e)).isEqualToIgnoringCase("n");
        assertThat(urlEncoder.urlEncodeChar((char) 0x7a)).isEqualToIgnoringCase("z");
    }

    @Test
    public void shouldNotEncodeExcludedChars() {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-zA-Z0-9]", Casing.NONE);

        assertThat(urlEncoder.urlEncodeChar((char) 0x00)).isEqualToIgnoringCase("%00");
        assertThat(urlEncoder.urlEncodeChar((char) 0x10)).isEqualToIgnoringCase("%10");
        assertThat(urlEncoder.urlEncodeChar((char) 0x1e)).isEqualToIgnoringCase("%1e");
        assertThat(urlEncoder.urlEncodeChar((char) 0x32)).isEqualToIgnoringCase("2");
        assertThat(urlEncoder.urlEncodeChar((char) 0x7a)).isEqualToIgnoringCase("z");
        assertThat(urlEncoder.urlEncodeChar((char) 0xff)).isEqualToIgnoringCase("%ff");
    }

    @Test
    public void shouldNotTranslateCaseIfNotEncodedChar() {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-zA-Z0-9]", Casing.UPPER);

        assertThat(urlEncoder.urlEncodeChar((char) 0x79)).isEqualTo("y");
        assertThat(urlEncoder.urlEncodeChar((char) 0x59)).isEqualTo("Y");
    }

    @Test
    public void shouldTranslateCaseIfEncodedChar() {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-zA-Z0-9]", Casing.UPPER);

        assertThat(urlEncoder.urlEncodeChar((char) 0xae)).isEqualTo("%AE");
    }

    @Test
    public void shouldEncode() {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-zA-Z0-9]", Casing.LOWER);
        final String nonEncoded = "a" + (char) 0x11 + "q" + (char) 0xf3;

        assertThat(urlEncoder.encode(nonEncoded)).isEqualTo("a%11q%f3");
    }

    @Test
    public void shouldEncode2() {
        final UrlEncoder urlEncoder = new UrlEncoder("", Casing.UPPER);
        final String nonEncoded = (char) 0xA2 + "" + (char) 0x11 + "" + (char) 0xf3;

        assertThat(urlEncoder.encode(nonEncoded)).isEqualTo("%A2%11%F3");
    }

}
