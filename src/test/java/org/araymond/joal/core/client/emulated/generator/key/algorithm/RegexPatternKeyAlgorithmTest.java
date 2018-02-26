package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegexPatternKeyAlgorithmTest {

    @Test
    public void shouldNotBuildWithNullLength() {
        assertThatThrownBy(() -> new RegexPatternKeyAlgorithm(null))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);

        assertThatThrownBy(() -> new RegexPatternKeyAlgorithm(""))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);
    }

    @Test
    public void shouldGeneratePeerIdMatchingPattern() {
        final RegexPatternKeyAlgorithm algo = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
    }

    @Test
    public void shouldBuild() {
        final RegexPatternKeyAlgorithm algo = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo.getPattern()).isEqualTo("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
    }

    @Test
    public void shouldBeEqualByPattern() {
        final RegexPatternKeyAlgorithm algo1 = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternKeyAlgorithm algo2 = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternKeyAlgorithm algo3 = new RegexPatternKeyAlgorithm("-qB33G0-[B-Ea-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo1)
                .isEqualTo(algo2)
                .isNotEqualTo(algo3);
    }

    @Test
    public void shouldHaveSameHashCodeByPattern() {
        final RegexPatternKeyAlgorithm algo1 = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternKeyAlgorithm algo2 = new RegexPatternKeyAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternKeyAlgorithm algo3 = new RegexPatternKeyAlgorithm("-qB33G0-[B-Ea-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo1.hashCode())
                .isEqualTo(algo2.hashCode())
                .isNotEqualTo(algo3.hashCode());
    }
    
}
