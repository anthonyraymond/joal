package org.araymond.joalcore.core.trackers.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AnnounceBackoffServiceTest {

    @Test
    public void shouldIncrementDelayForEachCall() {
        var backoff = new AnnounceBackoffService.DefaultBackoffService(250);

        var durations = IntStream.range(1, 10)
                .mapToObj(backoff::backoff)
                .toList();

        List<Duration> sorted = new ArrayList<>(durations);
        sorted.sort(Duration::compareTo);

        // if the original list is the same as the sorted one it means that the duration increase over time
        assertThat(durations).isEqualTo(sorted);
    }

    @Test
    public void shouldNotGoOverMaxRetryDelay() {
        var backoff = new AnnounceBackoffService.DefaultBackoffService(250);

        assertThat(backoff.backoff(Integer.MAX_VALUE)).isEqualTo(Duration.ofHours(1));
    }

    @Test
    public void shouldNotGoBelowMinRetryDelay() {
        var backoff = new AnnounceBackoffService.DefaultBackoffService(1);

        assertThat(backoff.backoff(1)).isGreaterThanOrEqualTo(Duration.ofSeconds(5));
    }

}