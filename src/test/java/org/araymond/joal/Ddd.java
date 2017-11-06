package org.araymond.joal;

import com.google.common.util.concurrent.*;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.announce.exceptions.TooMuchAnnouncesFailedInARawException;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Ddd implements AnnouncerManager {
    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
    private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, Future> torrentThreads = new HashMap<>();
    @Test
    public void d() throws InterruptedException {
        IntStream.range(0, 3)
                .forEach(i -> startNewAnnouncer());

        Thread.sleep(1200);

        Thread.sleep(1200);

        Thread.sleep(1200);

        Thread.sleep(1200);

        /*
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        callbackExecutor.shutdown();
        callbackExecutor.awaitTermination(10, TimeUnit.SECONDS);*/
    }

    public void startNewAnnouncer() {
        System.out.println("starting");
        if (!this.executor.isShutdown()) {
            // Get random torrent and store it in the list
            final String torrent = "some torrent";
            final RunAnotherAnnouncerCallback callback = new RunAnotherAnnouncerCallback(this, torrent);
            Futures.addCallback(executor.submit(new MyAnnouncer()), callback, this.callbackExecutor);
        }
    }

    public void deleteTorrent(final MockedTorrent torrent) {

    }

    private static class MyAnnouncer implements Callable<String> {
        @Override
        public String call() throws Exception {
            int counter = 0;
            while (counter < 333333) {
                System.out.println("Announcing" + Thread.currentThread().getName());
                try {
                    Thread.sleep(300);
                    ++counter;
                } catch (final InterruptedException ignored) {
                    break;
                }
            }
            System.out.println("Termianted gracefully" + Thread.currentThread().getName());
            return "ok";
        }
    }

    private static final class RunAnotherAnnouncerCallback implements FutureCallback<String> {

        private final AnnouncerManager announcerManager;
        private final String torrent;

        private RunAnotherAnnouncerCallback(final AnnouncerManager announcerManager, final String torrent) {
            this.announcerManager = announcerManager;
            this.torrent = torrent;
        }

        @Override
        public void onSuccess(@Nullable final String result) {
            announcerManager.startNewAnnouncer();
        }

        @Override
        public void onFailure(final Throwable t) {
            System.out.println("has failed");
            if (t instanceof TooMuchAnnouncesFailedInARawException) {
                final TooMuchAnnouncesFailedInARawException ex = (TooMuchAnnouncesFailedInARawException) t;
                announcerManager.deleteTorrent(ex.getTorrent());
            }
            announcerManager.startNewAnnouncer();
        }
    }
}
interface AnnouncerManager {
    void startNewAnnouncer();
    void deleteTorrent(final MockedTorrent torrent);
}
