package org.araymond.joal.core.bandwith.weight;

import org.araymond.joal.core.bandwith.Peers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Optional.ofNullable;

public class WeightHolder<E> {

    private final Lock lock;
    private final PeersAwareWeightCalculator weightCalculator;
    private final Map<E, Double> weightMap;
    private double totalWeight;

    public WeightHolder(final PeersAwareWeightCalculator weightCalculator) {
        this.weightCalculator = weightCalculator;
        this.weightMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void addOrUpdate(final E item, final Peers peers) {
        final double weight = this.weightCalculator.calculate(peers);
        lock.lock();
        try {
            ofNullable(this.weightMap.put(item, weight)).ifPresentOrElse(
                    previousWeight -> this.totalWeight = this.totalWeight - previousWeight + weight,
                    () -> this.totalWeight += weight);
        } finally {
            lock.unlock();
        }
    }

    public void remove(final E item) {
        lock.lock();
        try {
            ofNullable(this.weightMap.remove(item))
                    .ifPresent(w -> this.totalWeight -= w);
        } finally {
            lock.unlock();
        }
    }

    /**
     * For performance reasons, this method does not benefit from the lock.
     * That's not a big deal because:
     * - if a value is not yet added it will return 0.0.
     * - if a value is still present it will return the previous value.
     */
    public double getWeightFor(final E item) {
        return ofNullable(weightMap.get(item))
                .orElse(0.0);
    }

    public double getTotalWeight() {
        return totalWeight;
    }
}
