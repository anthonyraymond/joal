package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.response.AnnounceResponseCallback;

import java.util.*;
import java.util.concurrent.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class AnnouncerExecutor {

    private final AnnounceResponseCallback announceResponseCallback;
    private final ThreadPoolExecutor executorService;
    private final Map<InfoHash, AnnouncerWithFuture> currentlyRunning;

    public AnnouncerExecutor(final AnnounceResponseCallback announceResponseCallback) {
        this.announceResponseCallback = announceResponseCallback;
        // From javadoc :
        //   Unbounded queues. Using an unbounded queue (for example a LinkedBlockingQueue without a predefined capacity) will cause new tasks to wait in
        //   the queue when all corePoolSize threads are busy. Thus, no more than corePoolSize threads will ever be created. (And the value of the
        //   maximumPoolSize therefore doesn't have any effect.) This may be appropriate when each task is completely independent of others, so tasks
        //   cannot affect each others execution
        final int corePoolSize = 3;
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("annnouncer-%d").build();
        this.executorService = new ThreadPoolExecutor(corePoolSize, 3, 40, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), threadFactory);
        this.currentlyRunning = new ConcurrentHashMap<>();
    }

    public void execute(final AnnounceRequest request) {
        final Runnable task = () -> {
            try {
                announceResponseCallback.onAnnounceWillAnnounce(request.getEvent(), request.getAnnouncer());
                final SuccessAnnounceResponse result = request.getAnnouncer().announce(request.getEvent());
                announceResponseCallback.onAnnounceSuccess(request.getEvent(), request.getAnnouncer(), result);
            } catch (final TooManyAnnouncesFailedInARowException e) {
                announceResponseCallback.onTooManyAnnounceFailedInARow(request.getEvent(), request.getAnnouncer(), e);
            } catch (final Throwable throwable) {
                announceResponseCallback.onAnnounceFailure(request.getEvent(), request.getAnnouncer(), throwable);
            } finally {
                this.currentlyRunning.remove(request.getAnnouncer().getTorrentInfoHash());
            }
        };

        this.currentlyRunning.put(
                request.getAnnouncer().getTorrentInfoHash(),
                new AnnouncerWithFuture(request.getAnnouncer(), this.executorService.submit(task))
        );
    }

    public Optional<Announcer> deny(final InfoHash infoHash) {
        return ofNullable(this.currentlyRunning.remove(infoHash)).map(announcerFuture -> {
            announcerFuture.getFuture().cancel(true);
            return announcerFuture.getAnnouncer();
        });
    }

    public Set<Announcer> denyAll() {
        return new HashSet<>(this.currentlyRunning.keySet()).stream()
                .map(this::deny)
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    public void awaitForRunningTasks() {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("AnnouncerExecutor timed out after 10s");
            }
        } catch (final InterruptedException e) {
            log.warn("AnnouncerExecutor interrupt", e);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static final class AnnouncerWithFuture {
        private final Announcer announcer;
        private final Future<?> future;
    }
}
