package org.araymond.joalcore.core.trackers.domain;

import org.araymond.joalcore.annotations.concurency.Immutable;
import org.araymond.joalcore.annotations.ddd.DomainEntity;
import org.araymond.joalcore.annotations.ddd.ValueObject;

import java.time.Instant;

@DomainEntity
public class Tracker {
    private boolean announcing;
    private Instant nextAnnounceAt;
    private Counter consecutiveFails = new Counter();

    public Tracker() {
        announcing = false;
        nextAnnounceAt = Instant.now();
    }

    public boolean requireAnnounce(Instant at) {
        if (announcing) {
            return false;
        }
        if (nextAnnounceAt.isAfter(at)) {
            return false;
        }
        return true;
    }

    public void announce() {
        announcing = true;
    }

    public void announceSucceed(AnnounceSucceed response) {
        announcing = false;
        consecutiveFails = new Counter();

        nextAnnounceAt = Instant.now().plus(response.interval());
    }

    public void announceFailed(AnnounceFailed response, AnnounceBackoffService backoff) {
        announcing = false;
        consecutiveFails = consecutiveFails.increment();

        nextAnnounceAt = Instant.now().plus(backoff.backoff(consecutiveFails.count()));
    }

    @Immutable
    private static final class Counter {
        private final int count;

        public Counter() {
            count = 0;
        }

        private Counter(int count) {
            this.count = count;
        }

        public Counter increment() {
            if (count == Integer.MAX_VALUE) {
                return new Counter(count);
            }
            return new Counter(count + 1);
        }

        public int count() {
            return count;
        }
    }
}
