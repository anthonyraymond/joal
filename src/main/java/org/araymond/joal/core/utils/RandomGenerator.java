package org.araymond.joal.core.utils;

import java.util.Random;

/**
 * Created by raymo on 28/05/2017.
 */
public class RandomGenerator {

    private final Random rand;

    public RandomGenerator() {
        this.rand = new Random();
    }

    public Long nextLong(final Long min, final Long max) {
        return min + (long) (this.rand.nextDouble() * (max - min));
    }

}
