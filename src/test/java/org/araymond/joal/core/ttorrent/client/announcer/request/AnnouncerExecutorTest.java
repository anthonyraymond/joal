package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.response.AnnounceResponseCallback;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("AnonymousInnerClassMayBeStatic")
public class AnnouncerExecutorTest {

    @Test
    public void shouldNotExecuteMoreThanThreeConcurentThreads() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final AtomicInteger atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < 100; i++) {
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer() {
                        @Override
                        public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException {
                            atomicInteger.incrementAndGet();
                            try {
                                // Thread that nevers dies when started
                                Thread.sleep(90000);
                            } catch (final InterruptedException ignored) {
                            }
                            return null;
                        }
                    }),
                    new DefaultCallback()
            );
        }
        Thread.yield();
        Thread.sleep(50);

        assertThat(atomicInteger.get()).isEqualTo(3);
    }

    @Test
    public void shouldCallCallbackAfterExecution() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final CountDownLatch countDown = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer()),
                    new AnnounceResponseCallback() {
                        @Override
                        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                        }
                        @Override
                        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                            countDown.countDown();
                        }
                        @Override
                        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                        }
                        @Override
                        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                        }
                    }
            );
        }

        countDown.await(10, TimeUnit.SECONDS);
        assertThat(countDown.getCount()).isEqualTo(0L);
    }

    @Test
    public void shouldCallOnAnnounceFailureWhenAnnounceThrownException() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final CountDownLatch countDown = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer() {
                        @Override
                        public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException {
                            throw new RuntimeException("whoops");
                        }
                    }),
                    new AnnounceResponseCallback() {
                        @Override
                        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                        }
                        @Override
                        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                        }
                        @Override
                        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                            countDown.countDown();
                        }
                        @Override
                        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                        }
                    }
            );
        }

        countDown.await(10, TimeUnit.SECONDS);
        assertThat(countDown.getCount()).isEqualTo(0L);
    }

    @Test
    public void shouldCallTooManyFailsWhenAnnounceThrownTooManyFails() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final CountDownLatch countDown = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer() {
                        @Override
                        public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException, TooMuchAnnouncesFailedInARawException {
                            throw new TooMuchAnnouncesFailedInARawException(Mockito.mock(MockedTorrent.class));
                        }
                    }),
                    new AnnounceResponseCallback() {
                        @Override
                        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                        }
                        @Override
                        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                        }
                        @Override
                        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                        }
                        @Override
                        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                            countDown.countDown();
                        }
                    }
            );
        }

        countDown.await(10, TimeUnit.SECONDS);
        assertThat(countDown.getCount()).isEqualTo(0L);
    }

    @Test
    public void shouldDenyAThread() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Lock lock = new ReentrantLock();
        lock.lock(); //acquire lock to create deadlock in other threads

        executor.execute(
                AnnounceRequest.createRegular(new FakeAnnouncer() {
                    @Override
                    public InfoHash getTorrentInfoHash() {
                        return new InfoHash(new byte[] {0x22, 0x22, 0x23});
                    }

                    @Override
                    public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException {
                        lock.lock(); // deadlock to avoid methods to returns
                        return null;
                    }
                }),
                new AnnounceResponseCallback() {
                    @Override
                    public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                    }
                    @Override
                    public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                        atomicInteger.incrementAndGet();
                    }
                    @Override
                    public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                        atomicInteger.incrementAndGet();
                    }
                    @Override
                    public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                    }
                }
        );
        Thread.yield();
        Thread.sleep(50);

        // By creating a new InfoHash we also ensure that equals or hashcode is implemented properly in InfoHash
        final Optional<Announcer> denied = executor.deny(new InfoHash(new byte[]{0x22, 0x22, 0x23}));

        // Ensure the announcer is returned
        assertThat(denied).isPresent();
        // Ensure no callback have been called
        assertThat(atomicInteger.get()).isEqualTo(0);
    }

    @Test
    public void shouldReturnEmptyOptionalIfInfoHashDoesNotExists() {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final Optional<Announcer> denied = executor.deny(new InfoHash(new byte[]{0x22, 0x22, 0x23}));

        assertThat(denied).isEmpty();
    }

    @Test
    public void shouldDenyAll() throws InterruptedException {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Lock lock = new ReentrantLock();
        lock.lock(); //acquire lock to create deadlock in other threads

        for (int i = 0; i < 100; i++) {
            final int currentid = i;
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer() {
                        @Override
                        public InfoHash getTorrentInfoHash() {
                            return new InfoHash(ByteBuffer.allocate(4).putInt(currentid).array());
                        }

                        @Override
                        public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException {
                            lock.lock(); // deadlock to avoid methods to returns
                            return null;
                        }
                    }),
                    new AnnounceResponseCallback() {
                        @Override
                        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                        }
                        @Override
                        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                            atomicInteger.incrementAndGet();
                        }
                        @Override
                        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                            atomicInteger.incrementAndGet();
                        }
                        @Override
                        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                        }
                    }
            );
        }
        Thread.yield();
        Thread.sleep(50);

        assertThat(executor.denyAll()).hasSize(100);
        assertThat(atomicInteger.get()).isEqualTo(0);

        assertThat(executor.denyAll()).hasSize(0); // after being denied, the list of running thread should be empty
    }

    @Test
    public void shouldAwaitAllThreadToFinishBeforeReturningFromAwait() {
        final AnnouncerExecutor executor = new AnnouncerExecutor();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Lock lock = new ReentrantLock();
        lock.lock(); //acquire lock to create deadlock in other threads

        for (int i = 0; i < 15; i++) {
            final int currentid = i;
            executor.execute(
                    AnnounceRequest.createRegular(new FakeAnnouncer() {
                        @Override
                        public InfoHash getTorrentInfoHash() {
                            return new InfoHash(ByteBuffer.allocate(4).putInt(currentid).array());
                        }

                        @Override
                        public SuccessAnnounceResponse announce(final RequestEvent event) {
                            try {
                                Thread.sleep(10);
                                Thread.yield();
                            } catch (final InterruptedException e) {
                                throw new RuntimeException("Relay exception to the thread");
                            }
                            return null;
                        }
                    }),
                    new AnnounceResponseCallback() {
                        @Override
                        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
                        }
                        @Override
                        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
                            atomicInteger.incrementAndGet();
                        }
                        @Override
                        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
                        }
                        @Override
                        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
                        }
                    }
            );
        }

        executor.awaitForRunningTasks();
        assertThat(atomicInteger.get()).isEqualTo(15);
    }


    private static class FakeAnnouncer extends Announcer {
        FakeAnnouncer() {
            super(Mockito.mock(MockedTorrent.class), Mockito.mock(AnnounceDataAccessor.class));
        }

        @Override
        public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException, TooMuchAnnouncesFailedInARawException {
            return null;
        }
    }

    private static final class DefaultCallback implements AnnounceResponseCallback {
        @Override
        public void onAnnounceWillAnnounce(final RequestEvent event, final Announcer announcer) {
        }
        @Override
        public void onAnnounceSuccess(final RequestEvent event, final Announcer announcer, final SuccessAnnounceResponse result) {
        }
        @Override
        public void onAnnounceFailure(final RequestEvent event, final Announcer announcer, final Throwable throwable) {
        }

        @Override
        public void onTooManyAnnounceFailedInARaw(final RequestEvent event, final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        }
    }

}
