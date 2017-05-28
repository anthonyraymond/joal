package org.araymond.joal.core.utils;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 28/05/2017.
 */
public class RandomGeneratorTest {

    @Test
    public void shouldGenerateLongBetween() {
        final RandomGenerator gen = new RandomGenerator();

        final Long min = 130L;
        final Long max = 150L;
        IntStream.range(0, 100)
                .parallel()
                .forEach(i -> {
                    assertThat(gen.nextLong(min, max)).isBetween(min, max);
                });
    }

    @Test
    public void shouldGenerateLongAndStayInRange() {
        final RandomGenerator gen = new RandomGenerator();

        final Long min = 130L;
        final Long max = 131L;
        IntStream.range(0, 100)
                .parallel()
                .forEach(i -> {
                    assertThat(gen.nextLong(min, max))
                            .isBetween(min, max);
                });
    }

    @Test
    public void shouldGenerateLongEquals0() {
        final RandomGenerator gen = new RandomGenerator();

        final Long min = 0L;
        final Long max = 0L;
        IntStream.range(0, 100)
                .parallel()
                .forEach(i -> {
                    assertThat(gen.nextLong(min, max)).isEqualTo(0);
                });
    }

    @Test
    public void shouldGenerateLongWithMinEqualsMax() {
        final RandomGenerator gen = new RandomGenerator();

        final Long min = 130L;
        final Long max = 130L;
        IntStream.range(0, 100)
                .parallel()
                .forEach(i -> {
                    assertThat(gen.nextLong(min, max))
                            .isEqualTo(min)
                            .isEqualTo(max);
                });
    }

}
