package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.response.AnnounceResponseCallback;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;

import static org.slf4j.LoggerFactory.getLogger;

public class AnnouncerExecutor {
    private static final Logger logger = getLogger(AnnouncerExecutor.class);

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
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("announce-thread-%d").build();
        this.executorService = new ThreadPoolExecutor(corePoolSize, 3, 40, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), threadFactory);
        this.currentlyRunning = new HashMap<>(corePoolSize);
    }

    public void execute(final AnnounceRequest request) {
        final Callable<Void> callable = () -> {
            try {
                announceResponseCallback.onAnnounceWillAnnounce(request.getEvent(), request.getAnnouncer());
                final SuccessAnnounceResponse result = request.getAnnouncer().announce(request.getEvent());
                announceResponseCallback.onAnnounceSuccess(request.getEvent(), request.getAnnouncer(), result);
            } catch (final TooMuchAnnouncesFailedInARawException e) {
                announceResponseCallback.onTooManyAnnounceFailedInARaw(request.getEvent(), request.getAnnouncer(), e);
            } catch (final Throwable throwable) {
                announceResponseCallback.onAnnounceFailure(request.getEvent(), request.getAnnouncer(), throwable);
            } finally {
                this.currentlyRunning.remove(request.getAnnouncer().getTorrentInfoHash());
            }
            return null;
        };

        final Future<Void> future = this.executorService.submit(callable);

        this.currentlyRunning.put(
                request.getAnnouncer().getTorrentInfoHash(),
                new AnnouncerWithFuture(
                    request.getAnnouncer(),
                    future
                )
        );
    }

    public Optional<Announcer> deny(final InfoHash infoHash) {
        final AnnouncerWithFuture announcerWithFuture = this.currentlyRunning.get(infoHash);
        if (announcerWithFuture == null) {
            return Optional.empty();
        }
        announcerWithFuture.getFuture().cancel(true);
        this.currentlyRunning.remove(infoHash);

        return Optional.of(announcerWithFuture.getAnnouncer());
    }

    public List<Announcer> denyAll() {
        final Set<InfoHash> infoHashes = Sets.newHashSet(this.currentlyRunning.keySet());
        final List<Announcer> announcersCanceled = new ArrayList<>();

        for (final InfoHash infoHash: infoHashes) {
            final AnnouncerWithFuture announcerWithFuture = this.currentlyRunning.get(infoHash);
            if (announcerWithFuture != null) {
                announcerWithFuture.getFuture().cancel(true);
                this.currentlyRunning.remove(infoHash);
                announcersCanceled.add(announcerWithFuture.getAnnouncer());
            }
        }

        return announcersCanceled;
    }

    public void awaitForRunningTasks() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            logger.warn("AnnouncerExecutor has ended with timeout, some torrents was still trying to announce after 10s", e);
        }
    }

    private static final class AnnouncerWithFuture {
        private final Announcer announcer;
        private final Future<?> future;

        private AnnouncerWithFuture(final Announcer announcer, final Future<?> future) {
            this.announcer = announcer;
            this.future = future;
        }

        public Announcer getAnnouncer() {
            return announcer;
        }

        public Future<?> getFuture() {
            return future;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final AnnouncerWithFuture that = (AnnouncerWithFuture) o;
            return Objects.equal(announcer, that.announcer);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(announcer);
        }
    }

}
