package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class BandwidthDispatcherNotifierTest {

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldDoNothingOnWillAnnounce() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        notifier.onAnnouncerWillAnnounce(null, null);

        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldDoNothingOnStartFails() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        notifier.onAnnounceStartFails(null, null);

        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldDoNothingOnRegularFails() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        notifier.onAnnounceRegularFails(null, null);

        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @Test
    public void shouldDoNothingOnStopFails() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        notifier.onAnnounceStopFails(null, null);

        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldRegisterAndUpdateOnStartSuccess() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(10).when(successAnnounceResponse).getLeechers();
        doReturn(15).when(successAnnounceResponse).getSeeders();
        doReturn(100).when(successAnnounceResponse).getInterval();
        notifier.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        Mockito.verify(dispatcher, times(1)).registerTorrent(ArgumentMatchers.eq(infoHash));
        Mockito.verify(dispatcher, times(1)).updateTorrentPeers(ArgumentMatchers.eq(infoHash), ArgumentMatchers.eq(15), ArgumentMatchers.eq(10));
        Mockito.verify(dispatcher, times(1)).getSpeedMap();
        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldUpdateOnRegularSuccess() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(10).when(successAnnounceResponse).getLeechers();
        doReturn(15).when(successAnnounceResponse).getSeeders();
        doReturn(100).when(successAnnounceResponse).getInterval();
        notifier.onAnnounceRegularSuccess(announcer, successAnnounceResponse);

        Mockito.verify(dispatcher, times(1)).updateTorrentPeers(ArgumentMatchers.eq(infoHash), ArgumentMatchers.eq(15), ArgumentMatchers.eq(10));
        Mockito.verify(dispatcher, times(1)).getSpeedMap();
        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldUnregisterOnStopSuccess() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        notifier.onAnnounceStopSuccess(announcer, successAnnounceResponse);

        Mockito.verify(dispatcher, times(1)).unregisterTorrent(ArgumentMatchers.eq(infoHash));
        Mockito.verifyNoMoreInteractions(dispatcher);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "TypeMayBeWeakened"})
    @Test
    public void shouldUnregisterOnTooManyFailsInARow() {
        final BandwidthDispatcher dispatcher = mock(BandwidthDispatcher.class);

        final BandwidthDispatcherNotifier notifier = new BandwidthDispatcherNotifier(dispatcher);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        notifier.onTooManyAnnounceFailedInARow(announcer, new TooManyAnnouncesFailedInARowException(mock(MockedTorrent.class)));

        Mockito.verify(dispatcher, times(1)).unregisterTorrent(ArgumentMatchers.eq(infoHash));
        Mockito.verifyNoMoreInteractions(dispatcher);
    }

}
