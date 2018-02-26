package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HashKeyAlgorithmTest {

    @Test
    public void shouldNotBuildWithNullLength() {
        assertThatThrownBy(() -> new HashKeyAlgorithm(null))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);
    }

    @Test
    public void shouldGenerateValidHash() {
        final HashKeyAlgorithm algo = new HashKeyAlgorithm(8);

        assertThat(algo.generate()).matches("[0-9A-F]{8}");
        assertThat(algo.generate()).matches("[0-9A-F]{8}");
        assertThat(algo.generate()).matches("[0-9A-F]{8}");
        assertThat(algo.generate()).matches("[0-9A-F]{8}");
        assertThat(algo.generate()).matches("[0-9A-F]{8}");
    }

    @Test
    public void shouldBuild() {
        final HashKeyAlgorithm algo = new HashKeyAlgorithm(8);

        assertThat(algo.getLength()).isEqualTo(8);
    }

    @Test
    public void shouldBeEqualByLength() {
        final HashKeyAlgorithm algo1 = new HashKeyAlgorithm(8);
        final HashKeyAlgorithm algo2 = new HashKeyAlgorithm(8);
        final HashKeyAlgorithm algo3 = new HashKeyAlgorithm(6);

        assertThat(algo1)
                .isEqualTo(algo2)
                .isNotEqualTo(algo3);
    }

    @Test
    public void shouldHaveSameHashCodeByLength() {
        final HashKeyAlgorithm algo1 = new HashKeyAlgorithm(8);
        final HashKeyAlgorithm algo2 = new HashKeyAlgorithm(8);
        final HashKeyAlgorithm algo3 = new HashKeyAlgorithm(6);

        assertThat(algo1.hashCode())
                .isEqualTo(algo2.hashCode())
                .isNotEqualTo(algo3.hashCode());
    }

}
