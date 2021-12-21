package org.araymond.joal.core.client.emulated;

import org.junit.jupiter.api.Test;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 24/04/2017.
 */
public class HttpHeaderTest {

    @Test
    public void shouldBuild() {
        final HttpHeader header = new HttpHeader("Connection", "close");
        assertThat(header.getName()).isEqualTo("Connection");
        assertThat(header.getValue()).isEqualTo("close");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final HttpHeader header = new HttpHeader("Connection", "close");
        final HttpHeader header2 = new HttpHeader("Connection", "close");
        assertThat(header).isEqualTo(header2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final HttpHeader header = new HttpHeader("Connection", "close");
        final HttpHeader header2 = new HttpHeader("Connection", "close");
        assertThat(header.hashCode()).isEqualTo(header2.hashCode());
    }

}
