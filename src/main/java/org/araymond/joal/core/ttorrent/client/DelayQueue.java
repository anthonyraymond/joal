package org.araymond.joal.core.ttorrent.client;

import org.araymond.joal.core.torrent.torrent.InfoHash;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueue<T extends DelayQueue.InfoHashAble> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<IntervalAware<T>> queue = new PriorityQueue<>();

    /**
     * Add to item to the queue, and ensure item uniqueness into the queue.
     *
     * @param item
     * @param interval
     * @param unit
     */
    public void addOrReplace(final T item, final int interval, final TemporalUnit unit) {
        final IntervalAware<T> intervalAware = new IntervalAware<>(
                item,
                LocalDateTime.now().plus(interval, unit)
        );
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.queue.removeIf(i -> i.getItem().getInfoHash().equals(item.getInfoHash())); // Ensure no double will be present in the queue (don't ant to have two announce type for a torrent)
            this.queue.add(intervalAware);
        } finally {
            lock.unlock();
        }
    }

    public List<T> getAvailables() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final IntervalAware<T> first = queue.peek();
            final LocalDateTime now = LocalDateTime.now();
            if (first == null || first.releaseAt.isAfter(now)) {
                return Collections.emptyList();
            }

            final List<T> timedOutItems = new ArrayList<>();
            do {
                timedOutItems.add(this.queue.poll().getItem());
            } while (this.queue.size() > 0 && !this.queue.peek().releaseAt.isAfter(now));

            return timedOutItems;
        } finally {
            lock.unlock();
        }
    }

    public void remove(final T itemToRemove) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.queue.removeIf(item -> item.getItem().getInfoHash().equals(itemToRemove.getInfoHash()));
        } finally {
            lock.unlock();
        }
    }

    public List<T> drainAll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final List<T> items = new ArrayList<>(queue.size());
            while (queue.size() > 0) {
                items.add(queue.poll().getItem());
            }
            return items;
        } finally {
            lock.unlock();
        }
    }


    private static final class IntervalAware<T> implements Comparable<IntervalAware> {
        private final LocalDateTime releaseAt;
        private final T item;

        private IntervalAware(final T item, final LocalDateTime releaseAt) {
            this.item = item;
            this.releaseAt = releaseAt;
        }

        public T getItem() {
            return item;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(final IntervalAware o) {
            return this.releaseAt.compareTo(o.releaseAt);
        }
    }

    public interface InfoHashAble {
        InfoHash getInfoHash();
    }
}
