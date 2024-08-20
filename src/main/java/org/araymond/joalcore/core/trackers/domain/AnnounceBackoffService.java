package org.araymond.joalcore.core.trackers.domain;

import org.araymond.joalcore.annotations.DomainService;

import java.time.Duration;

@DomainService
public interface AnnounceBackoffService {
    Duration backoff(int consecutiveFails);

    class DefaultBackoffService implements AnnounceBackoffService {
        private static final Duration minimumRetryDelay = Duration.ofSeconds(5);
        private static final Duration maximumRetryDelay = Duration.ofHours(1);

        private final long backoffRatio;

        public DefaultBackoffService() {
            this(250);
        }

        public DefaultBackoffService(long backoffRatio) {
            if (backoffRatio < 0.0) {
                throw new IllegalArgumentException("backoffRatio must be greater than 0.0");
            }
            this.backoffRatio = backoffRatio;
        }

        @Override
        public Duration backoff(int consecutiveFails) {
            try {
                long failSquare = (long) consecutiveFails * consecutiveFails;

                var backoff = Duration.ofSeconds(failSquare)
                        .multipliedBy(backoffRatio)
                        .dividedBy(100);

                return min(
                        maximumRetryDelay,
                        minimumRetryDelay.plus(backoff)
                );
            } catch (ArithmeticException ignore) {
                return maximumRetryDelay;
            }
        }

        private Duration min(Duration d1, Duration d2) {
            return d1.compareTo(d2) < 0 ? d1 : d2;
        }
    }
}
