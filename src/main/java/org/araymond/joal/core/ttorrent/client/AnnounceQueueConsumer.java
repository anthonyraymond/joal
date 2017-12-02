package org.araymond.joal.core.ttorrent.client;

import com.google.common.util.concurrent.FutureCallback;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.TorrentAnnounceAware;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Whatever that would be used in eventhandlers of the queue HAVE to be thread safe since listener will be notified
 * (and thus executed) by threads.
 */
public class AnnounceQueueConsumer {

    private final AnnounceQueue announceQueue;
    private final ExecutorService executorService;
    private final ExecutorService callbackExecutorService;
    private TorrentAnnounceAware listener;


    public AnnounceQueueConsumer(final AnnounceQueue announceQueue) {
        this.executorService = Executors.newFixedThreadPool(3);
        this.callbackExecutorService = Executors.newSingleThreadExecutor();
        this.announceQueue = announceQueue;
    }





    private static class StartedAnnounceCallback implements FutureCallback<SuccessAnnounceResponse> {
        final Announcer announcer;
        final AnnounceQueue queue;

        private StartedAnnounceCallback(final Announcer announcer, final AnnounceQueue queue/*, bandwidthDispatcher */) {
            this.announcer = announcer;
            this.queue = queue;
        }

        @Override
        public void onSuccess(final SuccessAnnounceResponse result) {
            // TODO: publish announce success
            this.queue.addToInterval(this.announcer, result.getInterval());
            // TODO: bandwidthDispatcher.addTorrent(infohash, leechers, seeders);
        }

        @Override
        public void onFailure(final Throwable t) {
            // TODO: publish announce failed
            this.queue.addToStart(this.announcer);
        }
    }

    private static class RegularAnnounceCallback implements FutureCallback<SuccessAnnounceResponse> {
        final Announcer announcer;
        final AnnounceQueue queue;

        private RegularAnnounceCallback(final Announcer announcer, final AnnounceQueue queue/*, bandwidthDispatcher */) {
            this.announcer = announcer;
            this.queue = queue;
        }

        @Override
        public void onSuccess(final SuccessAnnounceResponse result) {
            // TODO: publish announce success
            this.queue.addToInterval(this.announcer, result.getInterval());
            // TODO: bandwidthDispatcher.updateTorrent(infohash, leechers, seeders);
        }

        @Override
        public void onFailure(final Throwable t) {
            // TODO: publish announce failed
            this.queue.addToInterval(this.announcer, this.announcer.getLastKnownInterval());
        }
    }

    private static class StoppedAnnounceCallback implements FutureCallback<SuccessAnnounceResponse> {
        final Announcer announcer;
        final AnnounceQueue queue;

        private StoppedAnnounceCallback(final Announcer announcer, final AnnounceQueue queue/*, bandwidthDispatcher */) {
            this.announcer = announcer;
            this.queue = queue;
        }

        @Override
        public void onSuccess(@Nullable final SuccessAnnounceResponse result) {
            // TODO: publish announce success
            // TODO: bandwidthDispatcher.removeTorrent(infohash);
        }

        @Override
        public void onFailure(final Throwable t) {
            // TODO: bandwidthDispatcher.removeTorrent(infohash);
        }
    }
}
