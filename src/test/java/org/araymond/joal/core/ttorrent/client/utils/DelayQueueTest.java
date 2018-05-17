package org.araymond.joal.core.ttorrent.client.utils;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
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

public class DelayQueueTest {

    public static DelayQueue.InfoHashAble createInfoHashAble(final String str) {
        return () -> new InfoHash(str.getBytes());
    }

    @Test
    public void shouldSort() {
        final DelayQueue<DelayQueue.InfoHashAble> queue = new DelayQueue<>();
        queue.addOrReplace(createInfoHashAble("two"), 20, ChronoUnit.SECONDS);
        queue.addOrReplace(createInfoHashAble("one"), 10, ChronoUnit.MILLIS);
        queue.addOrReplace(createInfoHashAble("four"), 1801, ChronoUnit.SECONDS);
        queue.addOrReplace(createInfoHashAble("three"), 30, ChronoUnit.MINUTES);

        final List<String> announcers = queue.drainAll().stream().map(i -> i.getInfoHash().value()).collect(Collectors.toList());;
        assertThat(announcers).containsExactly("one", "two", "three", "four");
        assertThat(queue.drainAll()).isEmpty();
    }

    @Test
    public void shouldNotBeAvailableBeforeIntervalTimeout() {
        final DelayQueue<DelayQueue.InfoHashAble> queue = new DelayQueue<>();

        queue.addOrReplace(createInfoHashAble("one"), -2, ChronoUnit.MILLIS);
        queue.addOrReplace(createInfoHashAble("two"), -1, ChronoUnit.MILLIS);
        queue.addOrReplace(createInfoHashAble("three"), 30, ChronoUnit.MINUTES);
        queue.addOrReplace(createInfoHashAble("four"), 1801, ChronoUnit.SECONDS);

        final List<String> announcers = queue.getAvailables().stream().map(i -> i.getInfoHash().value()).collect(Collectors.toList());
        assertThat(announcers).hasSize(2);
        assertThat(announcers).containsExactly("one", "two");
    }

    @Test
    public void shouldBeAbleToRemoveOneElement() {

        final DelayQueue<DelayQueue.InfoHashAble> queue = new DelayQueue<>();

        queue.addOrReplace(createInfoHashAble("one"), 20, ChronoUnit.MILLIS);
        queue.addOrReplace(createInfoHashAble("two"), 50, ChronoUnit.SECONDS);
        queue.addOrReplace(createInfoHashAble("three"), 30, ChronoUnit.MINUTES);

        queue.remove(createInfoHashAble("two"));

        final List<String> announcers = queue.drainAll().stream().map(i -> i.getInfoHash().value()).collect(Collectors.toList());
        assertThat(announcers)
                .hasSize(2)
                .containsExactly("one", "three");
    }

    @Test
    public void shouldBeThreadSafe() throws InterruptedException {
        final int announcerCount = 100;
        final DelayQueue<DelayQueue.InfoHashAble> queue = new DelayQueue<>();
        IntStream.range(0, announcerCount).forEach(i -> queue.addOrReplace(createInfoHashAble(String.valueOf(i)), -50, ChronoUnit.MILLIS));

        final List<Callable<List<DelayQueue.InfoHashAble>>> callables = IntStream.range(0, 5)
                .mapToObj(i -> (Callable<List<DelayQueue.InfoHashAble>>) queue::getAvailables)
                .collect(Collectors.toList());
        final ExecutorService executor = Executors.newFixedThreadPool(7);
        final List<Future<List<DelayQueue.InfoHashAble>>> futures = executor.invokeAll(callables);
        final List<DelayQueue.InfoHashAble> results = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (final Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        assertThat(results).hasSize(announcerCount);
    }

}
