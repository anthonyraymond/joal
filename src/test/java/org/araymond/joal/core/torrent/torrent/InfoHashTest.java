package org.araymond.joal.core.torrent.torrent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfoHashTest {

    public static InfoHash createOne(final String infoHash) {
        return new InfoHash(infoHash.getBytes());
    }

    @Test
    public void shouldBuild() {
        final InfoHash infoHash = new InfoHash("abcd".getBytes());

        assertThat(infoHash.value()).isEqualTo("abcd");
    }

    @Test
    public void shouldBeEqualsBeValue() {
        final InfoHash infoHash1 = new InfoHash("qdkjqsds".getBytes());
        final InfoHash infoHash2 = new InfoHash("qdkjqsds".getBytes());
        final InfoHash infoHash3 = new InfoHash("mlkdofgf".getBytes());

        assertThat(infoHash1)
                .isEqualTo(infoHash2)
                .isNotEqualTo(infoHash3);

        assertThat(infoHash1.hashCode())
                .isEqualTo(infoHash2.hashCode())
                .isNotEqualTo(infoHash3.hashCode());
    }

    @Test
    public void shouldReturnHexEncodedHumanReadable() {
        final InfoHash infoHash = new InfoHash(new byte[] {(byte) 0xab, (byte) 0xcd, (byte) 0x12, (byte) 0x34});

        assertThat(infoHash.getHumanReadable()).isEqualTo("abcd1234");
    }

}
