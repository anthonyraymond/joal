package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AnnounceReEnqueuerTest {

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldReEnqueueRegularIfRegularFail() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();
        doReturn(17).when(announcer).getLastKnownInterval();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onAnnounceRegularFails(announcer, new Exception("oops"));

        final ArgumentCaptor<AnnounceRequest> captor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.verify(delayQueue, times(1)).addOrReplace(captor.capture(), eq(17), eq(ChronoUnit.SECONDS));
        assertThat(captor.getValue().getEvent()).isEqualTo(RequestEvent.NONE);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldReEnqueueStartIfStartFail() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();
        doReturn(17).when(announcer).getLastKnownInterval();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onAnnounceStartFails(announcer, new Exception("oops"));

        final ArgumentCaptor<AnnounceRequest> captor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.verify(delayQueue, times(1)).addOrReplace(captor.capture(), eq(17), eq(ChronoUnit.SECONDS));
        assertThat(captor.getValue().getEvent()).isEqualTo(RequestEvent.STARTED);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldReEnqueueStopWithZeroSecondsDelayIfStopFail() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();
        doReturn(17).when(announcer).getLastKnownInterval();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onAnnounceStopFails(announcer, new Exception("oops"));

        final ArgumentCaptor<AnnounceRequest> captor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.verify(delayQueue, times(1)).addOrReplace(captor.capture(), eq(0), eq(ChronoUnit.SECONDS));
        assertThat(captor.getValue().getEvent()).isEqualTo(RequestEvent.STOPPED);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldReEnqueueRegularIfStartSuccess() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();
        doReturn(17).when(announcer).getLastKnownInterval();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        final SuccessAnnounceResponse successAnnounceResponse = Mockito.mock(SuccessAnnounceResponse.class);
        doReturn(150).when(successAnnounceResponse).getInterval();
        announceReEnqueuer.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        final ArgumentCaptor<AnnounceRequest> captor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.verify(delayQueue, times(1)).addOrReplace(captor.capture(), eq(150), eq(ChronoUnit.SECONDS));
        assertThat(captor.getValue().getEvent()).isEqualTo(RequestEvent.NONE);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldReEnqueueRegularIfRegularSuccess() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();
        doReturn(17).when(announcer).getLastKnownInterval();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        final SuccessAnnounceResponse successAnnounceResponse = Mockito.mock(SuccessAnnounceResponse.class);
        doReturn(150).when(successAnnounceResponse).getInterval();
        announceReEnqueuer.onAnnounceRegularSuccess(announcer, successAnnounceResponse);

        final ArgumentCaptor<AnnounceRequest> captor = ArgumentCaptor.forClass(AnnounceRequest.class);

        Mockito.verify(delayQueue, times(1)).addOrReplace(captor.capture(), eq(150), eq(ChronoUnit.SECONDS));
        assertThat(captor.getValue().getEvent()).isEqualTo(RequestEvent.NONE);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldDoNothingOnStopSuccess() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onAnnounceStopSuccess(announcer, Mockito.mock(SuccessAnnounceResponse.class));

        Mockito.verifyNoMoreInteractions(delayQueue);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldDoNothingOnWillAnnounce() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onAnnouncerWillAnnounce(announcer, RequestEvent.STARTED);

        Mockito.verifyNoMoreInteractions(delayQueue);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldDoNothingOnTooManyFails() {
        @SuppressWarnings("unchecked") final DelayQueue<AnnounceRequest> delayQueue = mock(DelayQueue.class);
        final Announcer announcer = mock(Announcer.class);
        Mockito.doReturn(new InfoHash("ddd".getBytes())).when(announcer).getTorrentInfoHash();

        final AnnounceReEnqueuer announceReEnqueuer = new AnnounceReEnqueuer(delayQueue);

        announceReEnqueuer.onTooManyAnnounceFailedInARow(announcer, new TooManyAnnouncesFailedInARowException(Mockito.mock(MockedTorrent.class)));

        Mockito.verifyNoMoreInteractions(delayQueue);
    }

}
