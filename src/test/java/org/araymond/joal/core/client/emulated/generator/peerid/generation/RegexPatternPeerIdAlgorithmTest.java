package org.araymond.joal.core.client.emulated.generator.peerid.generation;

import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegexPatternPeerIdAlgorithmTest {

    @Test
    public void shouldNotBuildWithoutPattern() {
        assertThatThrownBy(() -> new RegexPatternPeerIdAlgorithm(null))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);
    }

    @Test
    public void shouldGeneratePeerIdMatchingPattern() {
        final RegexPatternPeerIdAlgorithm algo = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        assertThat(algo.generate()).matches("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
    }

    @Test
    public void shouldBuild() {
        final RegexPatternPeerIdAlgorithm algo = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo.getPattern()).isEqualTo("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
    }

    @Test
    public void shouldBeEqualByPattern() {
        final RegexPatternPeerIdAlgorithm algo1 = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternPeerIdAlgorithm algo2 = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternPeerIdAlgorithm algo3 = new RegexPatternPeerIdAlgorithm("-qB33G0-[B-Ea-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo1)
                .isEqualTo(algo2)
                .isNotEqualTo(algo3);
    }

    @Test
    public void shouldHaveSameHashCodeByPattern() {
        final RegexPatternPeerIdAlgorithm algo1 = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternPeerIdAlgorithm algo2 = new RegexPatternPeerIdAlgorithm("-qB33G0-[A-Za-z0-9_~\\(\\)\\!\\.\\*-]{12}");
        final RegexPatternPeerIdAlgorithm algo3 = new RegexPatternPeerIdAlgorithm("-qB33G0-[B-Ea-z0-9_~\\(\\)\\!\\.\\*-]{12}");

        assertThat(algo1.hashCode())
                .isEqualTo(algo2.hashCode())
                .isNotEqualTo(algo3.hashCode());
    }

}
