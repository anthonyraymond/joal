package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HashNoLeadingZeroKeyAlgorithmTest {

    @Test
    public void shouldNotBuildWithNullLength() {
        assertThatThrownBy(() -> new HashNoLeadingZeroKeyAlgorithm(null))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);
    }

    @Test
    public void shouldGenerateValidHash() {
        final HashNoLeadingZeroKeyAlgorithm algo = new HashNoLeadingZeroKeyAlgorithm(8);

        assertThat(algo.generate()).matches("^[0-9A-F]*$");
        assertThat(algo.generate()).matches("^[0-9A-F]*$");
        assertThat(algo.generate()).matches("^[0-9A-F]*$");
        assertThat(algo.generate()).matches("^[0-9A-F]*$");
        assertThat(algo.generate()).matches("^[0-9A-F]*$");
    }

    @Test
    public void shouldCallRemoveTrailingZero() {
        final HashNoLeadingZeroKeyAlgorithm algo = Mockito.spy(new HashNoLeadingZeroKeyAlgorithm(8));

        algo.generate();

        Mockito.verify(algo, Mockito.times(1)).removeLeadingZeroes(ArgumentMatchers.anyString());
    }

    @Test
    public void shouldRemoveTrailingZeros() {
        final HashNoLeadingZeroKeyAlgorithm algo = new HashNoLeadingZeroKeyAlgorithm(8);

        assertThat(algo.removeLeadingZeroes("00AF32020")).isEqualTo("AF32020");
    }

    @Test
    public void shouldBuild() {
        final HashNoLeadingZeroKeyAlgorithm algo = new HashNoLeadingZeroKeyAlgorithm(8);

        assertThat(algo.getLength()).isEqualTo(8);
    }

    @Test
    public void shouldBeEqualByLength() {
        final HashNoLeadingZeroKeyAlgorithm algo1 = new HashNoLeadingZeroKeyAlgorithm(8);
        final HashNoLeadingZeroKeyAlgorithm algo2 = new HashNoLeadingZeroKeyAlgorithm(8);
        final HashNoLeadingZeroKeyAlgorithm algo3 = new HashNoLeadingZeroKeyAlgorithm(6);

        assertThat(algo1)
                .isEqualTo(algo2)
                .isNotEqualTo(algo3);
    }

    @Test
    public void shouldHaveSameHashCodeByLength() {
        final HashNoLeadingZeroKeyAlgorithm algo1 = new HashNoLeadingZeroKeyAlgorithm(8);
        final HashNoLeadingZeroKeyAlgorithm algo2 = new HashNoLeadingZeroKeyAlgorithm(8);
        final HashNoLeadingZeroKeyAlgorithm algo3 = new HashNoLeadingZeroKeyAlgorithm(6);

        assertThat(algo1.hashCode())
                .isEqualTo(algo2.hashCode())
                .isNotEqualTo(algo3.hashCode());
    }


}
