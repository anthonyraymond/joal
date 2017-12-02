package org.araymond.joal.core.ttorrent.client.utils;

import com.google.common.base.Objects;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AvailableAfterIntervalQueue<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<IntervalAware<T>> queue = new PriorityQueue<>();

    public void add(final T item, final int interval, final TemporalUnit unit) {
        final IntervalAware<T> intervalAware = new IntervalAware<>(
                item,
                LocalDateTime.now().plus(interval, unit)
        );
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.queue.add(intervalAware);
        } finally {
            lock.unlock();
        }
    }

    public List<T> getAvailable() {
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
            this.queue.removeIf(item -> item.getItem().equals(itemToRemove));
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

        @Override
        public int compareTo(final IntervalAware o) {
            return this.releaseAt.compareTo(o.releaseAt);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final IntervalAware<?> that = (IntervalAware<?>) o;
            return Objects.equal(releaseAt, that.releaseAt) &&
                    Objects.equal(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(releaseAt, item);
        }
    }
}
