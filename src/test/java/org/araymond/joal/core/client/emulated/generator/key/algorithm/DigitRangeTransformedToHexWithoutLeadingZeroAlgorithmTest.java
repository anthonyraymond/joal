package org.araymond.joal.core.client.emulated.generator.key.algorithm;

import org.araymond.joal.core.client.emulated.TorrentClientConfigIntegrityException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

public class DigitRangeTransformedToHexWithoutLeadingZeroAlgorithmTest {

    @Test
    public void shouldNotBuildWithLowerBoundGreaterThanUpperBound() {
        assertThatThrownBy(() -> new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(100L, 1L))
                .isInstanceOf(TorrentClientConfigIntegrityException.class);
    }

    @Test
    public void shouldBuild() {
        try {
            final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 16800L);
        } catch (Exception e) {
            fail("should not have failed to build", e);
        }
    }

    @Test
    public void shouldGenerateHashRepresentationOfLong() {
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algorithm = Mockito.spy(new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 16800L));
        doReturn(150L)
                .doReturn(190946470L)
                .doReturn(4294967295L)
                .when(algorithm).getRandomDigitBetween(anyLong(), anyLong());

        assertThat(algorithm.generate()).isEqualToIgnoringCase("96");
        assertThat(algorithm.generate()).isEqualToIgnoringCase("B619CA6");
        assertThat(algorithm.generate()).isEqualToIgnoringCase("FFFFFFFF");
    }



    @Test
    public void shouldBeEqualByLowerAndUpperBounds() {
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo1 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 10L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo2 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 10L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo3 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 1L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo4 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(10L, 100L);

        assertThat(algo1)
                .isEqualTo(algo2)
                .isNotEqualTo(algo3)
                .isNotEqualTo(algo4);
    }

    @Test
    public void shouldHaveSameHashCodeByLength() {
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo1 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 10L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo2 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 10L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo3 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(0L, 1L);
        final DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm algo4 = new DigitRangeTransformedToHexWithoutLeadingZeroAlgorithm(10L, 100L);

        assertThat(algo1.hashCode())
                .isEqualTo(algo2.hashCode())
                .isNotEqualTo(algo3.hashCode())
                .isNotEqualTo(algo4.hashCode());
    }

}
