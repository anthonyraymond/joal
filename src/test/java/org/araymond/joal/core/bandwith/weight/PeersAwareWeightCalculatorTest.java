package org.araymond.joal.core.bandwith.weight;

import org.araymond.joal.core.bandwith.Peers;
import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PeersAwareWeightCalculatorTest {

    @Test
    public void shouldNeverGoBelowZero() {
        final PeersAwareWeightCalculator calculator = new PeersAwareWeightCalculator();
        assertThat(calculator.calculate(new Peers(0, 0))).isEqualTo(0);
        assertThat(calculator.calculate(new Peers(0, 1))).isEqualTo(0);
        assertThat(calculator.calculate(new Peers(1, 0))).isEqualTo(0);
    }

    @Test
    public void shouldPromoteTorrentWithMoreLeechers() {
        final PeersAwareWeightCalculator calculator = new PeersAwareWeightCalculator();

        final double first = calculator.calculate(new Peers(10, 10));
        final double second = calculator.calculate(new Peers(10, 30));
        final double third = calculator.calculate(new Peers(10, 100));
        final double fourth = calculator.calculate(new Peers(10, 200));

        assertThat(fourth)
                .isGreaterThan(third)
                .isGreaterThan(second)
                .isGreaterThan(first);
    }

    @Test
    public void shouldProvideExactValues() {
        final PeersAwareWeightCalculator calculator = new PeersAwareWeightCalculator();

        assertThat(calculator.calculate(new Peers(1, 1))).isEqualTo(25);
        assertThat(calculator.calculate(new Peers(2, 1))).isCloseTo(11.1, Offset.offset(0.1));
        assertThat(calculator.calculate(new Peers(30, 1))).isCloseTo(0.104058273, Offset.offset(0.00000001));
        assertThat(calculator.calculate(new Peers(0, 1))).isEqualTo(0);
        assertThat(calculator.calculate(new Peers(1, 0))).isEqualTo(0);
        assertThat(calculator.calculate(new Peers(2, 100))).isCloseTo(9611.687812, Offset.offset(0.0001));
        assertThat(calculator.calculate(new Peers(0, 100))).isEqualTo(0);
        assertThat(calculator.calculate(new Peers(2000, 150))).isEqualTo(73.01243916, Offset.offset(0.00001));
        assertThat(calculator.calculate(new Peers(150, 2000))).isCloseTo(173066.5224, Offset.offset(0.01));
        assertThat(calculator.calculate(new Peers(80, 2000))).isCloseTo(184911.2426, Offset.offset(0.1));
        assertThat(calculator.calculate(new Peers(2000, 2000))).isEqualTo(50000);
        assertThat(calculator.calculate(new Peers(0, 0))).isEqualTo(0);
    }

}
