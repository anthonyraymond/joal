package org.araymond.joal.core.ttorrent.client.announcer.utils;

import org.araymond.joal.core.ttorrent.client.announcer.utils.AvailableAfterIntervalQueue;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AvailableAfterIntervalQueueTest {


    @Test
    public void shouldSort() {
        final AvailableAfterIntervalQueue<String> queue = new AvailableAfterIntervalQueue<>();
        queue.add("two", 20, ChronoUnit.SECONDS);
        queue.add("one", 10, ChronoUnit.MILLIS);
        queue.add("four", 1801, ChronoUnit.SECONDS);
        queue.add("three", 30, ChronoUnit.MINUTES);

        final List<String> announcers = queue.drainAll();
        assertThat(announcers).containsExactly("one", "two", "three", "four");
        assertThat(queue.drainAll()).isEmpty();
    }

    @Test
    public void shouldNotBeAvailableBeforeIntervalTimeout() {
        final AvailableAfterIntervalQueue<String> queue = new AvailableAfterIntervalQueue<>();

        queue.add("one", -2, ChronoUnit.MILLIS);
        queue.add("two", -1, ChronoUnit.MILLIS);
        queue.add("three", 30, ChronoUnit.MINUTES);
        queue.add("four", 1801, ChronoUnit.SECONDS);

        final List<String> announcers = queue.getAvailable();
        assertThat(announcers).hasSize(2);
        assertThat(announcers).containsExactly("one", "two");
    }

    @Test
    public void shouldBeAbleToRemoveOneElement() {

        final AvailableAfterIntervalQueue<String> queue = new AvailableAfterIntervalQueue<>();

        queue.add("one", 20, ChronoUnit.MILLIS);
        queue.add("two", 50, ChronoUnit.SECONDS);
        queue.add("three", 30, ChronoUnit.MINUTES);

        queue.remove("two");

        final List<String> announcers = queue.drainAll();
        assertThat(announcers)
                .hasSize(2)
                .containsExactly("one", "three");
    }

    @Test
    public void shouldBeThreadSafe() throws InterruptedException {
        final int threadCount = 100;
        final AvailableAfterIntervalQueue<String> queue = new AvailableAfterIntervalQueue<>();
        IntStream.range(0, threadCount).forEach(i -> queue.add(String.valueOf(i), -50, ChronoUnit.MILLIS));

        final List<Callable<List<String>>> callables = IntStream.range(0, threadCount)
                .mapToObj(i -> (Callable<List<String>>) queue::getAvailable)
                .collect(Collectors.toList());
        final ExecutorService executor = Executors.newFixedThreadPool(7);
        final List<Future<List<String>>> futures = executor.invokeAll(callables);
        final List<String> results = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (final Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        assertThat(results).hasSize(threadCount);
    }

}
