package org.araymond.joalcore.core.metadata.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InfoHashTest {
    @Test
    public void shouldBeEqualByValue() {
        assertThat(new InfoHash(new byte[]{0, 25, 127})).isEqualTo(new InfoHash(new byte[]{0, 25, 127}));
    }

    @Test
    public void hashCodeShouldBeEqualByValue() {
        assertThat(new InfoHash(new byte[]{0, 25, 127}).hashCode()).isEqualTo(new InfoHash(new byte[]{0, 25, 127}).hashCode());
    }

    @Test
    public void shouldWriteToStringHex() {
        assertThat(new InfoHash(new byte[]{(byte)0, (byte)25, (byte)255}).hexInfoHash()).isEqualTo("0019FF");
    }
}