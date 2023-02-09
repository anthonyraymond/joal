package org.araymond.joal.core.bandwith.weight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.bandwith.Peers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Optional.ofNullable;

/**
 * Keeps track of the 'weights' of each and every torrent we're currently processing/uploading.
 * These weights will be used (likely by {@link BandwidthDispatcher}) to calculate per-torrent
 * speeds from our global configured bandwidth budget.
 */
@RequiredArgsConstructor
public class WeightHolder<E> {

    private final Lock lock = new ReentrantLock();
    private final Map<E, Double> weightMap = new HashMap<>();
    private final PeersAwareWeightCalculator weightCalculator;
    @Getter private double totalWeight;

    public void addOrUpdate(final E item, final Peers peers, final long torrentSize, final long uploaded) {
        final double weight = this.weightCalculator.calculate(peers, torrentSize, uploaded);
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
     * - if a value is present it will return the current value.
     */
    public double getWeightFor(final E item) {
        return weightMap.getOrDefault(item, 0.0);
    }
}
