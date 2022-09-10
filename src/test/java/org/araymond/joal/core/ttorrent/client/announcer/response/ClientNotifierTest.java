package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.Client;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ClientNotifierTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotifyNoMorePeersIfAnnounceStartedResponseHasNoLeechers() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(0).when(successAnnounceResponse).getLeechers();
        doReturn(100).when(successAnnounceResponse).getSeeders();
        clientNotifier.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        Mockito.verify(client, times(1)).onNoMorePeers(infoHash);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotifyNoMorePeersIfAnnounceStartedResponseHasNoSeeders() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(100).when(successAnnounceResponse).getLeechers();
        doReturn(0).when(successAnnounceResponse).getSeeders();

        clientNotifier.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        Mockito.verify(client, times(1)).onNoMorePeers(infoHash);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingIfAnnounceStartedHasPeers() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(1).when(successAnnounceResponse).getLeechers();
        doReturn(1).when(successAnnounceResponse).getSeeders();

        clientNotifier.onAnnounceStartSuccess(announcer, successAnnounceResponse);

        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotifyNoMorePeersIfAnnounceRegularResponseHasNoLeechers() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(0).when(successAnnounceResponse).getLeechers();
        doReturn(100).when(successAnnounceResponse).getSeeders();
        clientNotifier.onAnnounceRegularSuccess(announcer, successAnnounceResponse);

        Mockito.verify(client, times(1)).onNoMorePeers(infoHash);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotifyNoMorePeersIfAnnounceRegularResponseHasNoSeeders() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(100).when(successAnnounceResponse).getLeechers();
        doReturn(0).when(successAnnounceResponse).getSeeders();

        clientNotifier.onAnnounceRegularSuccess(announcer, successAnnounceResponse);

        Mockito.verify(client, times(1)).onNoMorePeers(infoHash);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingIfAnnounceRegularHasPeers() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final InfoHash infoHash = new InfoHash("qjfqjbqdui".getBytes());
        final Announcer announcer = mock(Announcer.class);
        doReturn(infoHash).when(announcer).getTorrentInfoHash();
        final SuccessAnnounceResponse successAnnounceResponse = mock(SuccessAnnounceResponse.class);
        doReturn(1).when(successAnnounceResponse).getLeechers();
        doReturn(1).when(successAnnounceResponse).getSeeders();

        clientNotifier.onAnnounceRegularSuccess(announcer, successAnnounceResponse);
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void shouldNotifyTorrentHasStoppedOnStopSuccess() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final Announcer announcer = mock(Announcer.class);

        clientNotifier.onAnnounceStopSuccess(announcer, mock(SuccessAnnounceResponse.class));

        Mockito.verify(client, times(1)).onTorrentHasStopped(ArgumentMatchers.eq(announcer));
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void shouldNotifyTorrentFailedTooManyTimes() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        final Announcer announcer = mock(Announcer.class);

        clientNotifier.onTooManyAnnounceFailedInARow(announcer, new TooManyAnnouncesFailedInARowException(mock(MockedTorrent.class)));

        Mockito.verify(client, times(1)).onTooManyFailedInARaw(ArgumentMatchers.eq(announcer));
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingOnWillAnnounce() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        clientNotifier.onAnnouncerWillAnnounce(null, null);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingOnStartFails() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        clientNotifier.onAnnounceStartFails(null, null);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingOnRegularFails() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        clientNotifier.onAnnounceRegularFails(null, null);
        Mockito.verifyNoMoreInteractions(client);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldDoNothingOnStopFailes() {
        final Client client = mock(Client.class);
        final ClientNotifier clientNotifier = new ClientNotifier(client);

        clientNotifier.onAnnounceStopFails(null, null);
        Mockito.verifyNoMoreInteractions(client);
    }

}
