package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import com.turn.ttorrent.common.protocol.http.HTTPAnnounceRequestMessage;
import com.turn.ttorrent.common.protocol.http.HTTPAnnounceResponseMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerErrorMessage;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResponseListener;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 02/06/2017.
 */
public class TrackerClientTest {

    @Test
    public void shouldNotBuildWithoutTorrent() throws URISyntaxException {
        final TorrentWithStats torrent = null;
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, connectionHandler, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldNotBuildWithoutConnectionhandler() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = null;
        final URI uri = new URI("http://example.tracker.com/announce");

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, connectionHandler, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("ConnectionHandler must not be null.");
    }

    @Test
    public void shouldNotBuildWithoutURI() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = null;

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, connectionHandler, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("URI must not be null.");
    }

    @Test
    public void shouldBuild() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, connectionHandler, uri);

        assertThat(trackerClient.getTorrentWithStats()).isEqualTo(torrent);
        assertThat(trackerClient.getConnectionHandler()).isEqualTo(connectionHandler);
        assertThat(trackerClient.getTrackerURI()).isEqualTo(uri);
    }

    @Test
    public void shouldThrowAnnounceExceptionWhenMessageIsAnErrorMessageOnHandleTrackerResponse() throws URISyntaxException, IOException, TrackerMessage.MessageValidationException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, connectionHandler, uri);

        final TrackerMessage.ErrorMessage message = HTTPTrackerErrorMessage.craft(TrackerMessage.ErrorMessage.FailureReason.UNKNOWN_TORRENT.getMessage());

        assertThatThrownBy(() -> trackerClient.handleTrackerAnnounceResponse((TrackerMessage) message))
                .isInstanceOf(AnnounceException.class)
                .hasMessage(TrackerMessage.ErrorMessage.FailureReason.UNKNOWN_TORRENT.getMessage());
    }

    @Test
    public void shouldThrowAnnounceExceptionWhenMessageIsNotAnErrorOrResponseMessageOnHandleTrackerResponse() throws URISyntaxException, IOException, TrackerMessage.MessageValidationException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, connectionHandler, uri);

        final TrackerMessage message = Mockito.mock(HTTPAnnounceRequestMessage.class);
        Mockito.when(message.getType()).thenReturn(TrackerMessage.Type.ANNOUNCE_REQUEST);

        assertThatThrownBy(() -> trackerClient.handleTrackerAnnounceResponse(message))
                .isInstanceOf(AnnounceException.class)
                .hasMessage("Unexpected tracker message type ANNOUNCE_REQUEST!");
    }

    @Test
    public void shouldNotifyListenerOnHandleTrackerResponse() throws URISyntaxException, AnnounceException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, connectionHandler, uri);
        final DefaultResponseListener listener = Mockito.spy(new DefaultResponseListener(new CountDownLatch(1), new CountDownLatch(1)));
        trackerClient.register(listener);

        final HTTPAnnounceResponseMessage message = Mockito.mock(HTTPAnnounceResponseMessage.class);
        Mockito.when(message.getComplete()).thenReturn(1560);
        Mockito.when(message.getIncomplete()).thenReturn(54676865);
        Mockito.when(message.getInterval()).thenReturn(1800);
        Mockito.when(message.getPeers()).thenReturn(Lists.emptyList());

        trackerClient.handleTrackerAnnounceResponse(message);

        assertThat(listener.getAnnounceResponseCountDown().getCount()).isEqualTo(0);
        assertThat(listener.getDiscoverPeerCountDown().getCount()).isEqualTo(0);
        Mockito.verify(listener, Mockito.times(1)).handleAnnounceResponse(torrent);
        Mockito.verify(listener, Mockito.times(1)).handleDiscoveredPeers(
                Matchers.eq(torrent),
                Matchers.anyListOf(Peer.class)
        );
    }

    @Test
    public void shouldAnnounce() throws URISyntaxException, AnnounceException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final HTTPAnnounceResponseMessage message = Mockito.mock(HTTPAnnounceResponseMessage.class);
        Mockito.when(message.getComplete()).thenReturn(1560);
        Mockito.when(message.getIncomplete()).thenReturn(54676865);
        Mockito.when(message.getInterval()).thenReturn(1800);
        Mockito.when(message.getPeers()).thenReturn(Lists.emptyList());

        final DefaultTrackerClient trackerClient = Mockito.spy(new DefaultTrackerClient(torrent, connectionHandler, uri));
        Mockito.when(trackerClient.makeCallAndGetResponseAsByteBuffer(Mockito.any(RequestEvent.class))).thenReturn(message);

        final DefaultResponseListener listener = Mockito.spy(new DefaultResponseListener(new CountDownLatch(1), new CountDownLatch(1)));
        trackerClient.register(listener);

        trackerClient.announce(RequestEvent.NONE);
        Mockito.verify(listener, Mockito.times(1)).handleAnnounceResponse(torrent);
        Mockito.verify(listener, Mockito.times(1)).handleDiscoveredPeers(
                Matchers.eq(torrent),
                Matchers.anyListOf(Peer.class)
        );
    }

    @Test
    public void shouldFormatAnnounce() {
        final DefaultTrackerClient trackerClient = Mockito.mock(DefaultTrackerClient.class);
        Mockito.when(trackerClient.formatAnnounceEvent(Matchers.any())).thenCallRealMethod();

        assertThat(trackerClient.formatAnnounceEvent(RequestEvent.NONE)).isEmpty();
        assertThat(trackerClient.formatAnnounceEvent(RequestEvent.STARTED)).isEqualTo(RequestEvent.STARTED.name());
        assertThat(trackerClient.formatAnnounceEvent(RequestEvent.STOPPED)).isEqualTo(RequestEvent.STOPPED.name());
        assertThat(trackerClient.formatAnnounceEvent(RequestEvent.COMPLETED)).isEqualTo(RequestEvent.COMPLETED.name());
    }

    @Test
    public void shouldDoNothingOnClose() {
        final DefaultTrackerClient trackerClient = Mockito.mock(DefaultTrackerClient.class);
        Mockito.doCallRealMethod().when(trackerClient).close();
        try {
            trackerClient.close();
        } catch (final Throwable ignored) {
            fail("should not fail to close.");
        }
    }

    private static class DefaultTrackerClient extends TrackerClient {

        DefaultTrackerClient(final TorrentWithStats torrent, final ConnectionHandler connectionHandler, final URI tracker) {
            super(torrent, connectionHandler, tracker);
        }

        @Override
        protected TrackerMessage makeCallAndGetResponseAsByteBuffer(final RequestEvent event) throws AnnounceException {
            return null;
        }

        TorrentWithStats getTorrentWithStats() {
            return this.torrent;
        }

        ConnectionHandler getConnectionHandler() {
            return this.connectionHandler;
        }

    }

    private static class DefaultResponseListener implements AnnounceResponseListener {
        private final CountDownLatch announceResponseCountDown;
        private final CountDownLatch discoverPeerCountDown;

        private DefaultResponseListener(final CountDownLatch announceResponseCountDown, final CountDownLatch discoverPeerCountDown) {
            this.announceResponseCountDown = announceResponseCountDown;
            this.discoverPeerCountDown = discoverPeerCountDown;
        }

        public CountDownLatch getAnnounceResponseCountDown() {
            return announceResponseCountDown;
        }

        public CountDownLatch getDiscoverPeerCountDown() {
            return discoverPeerCountDown;
        }

        @Override
        public void handleAnnounceResponse(final TorrentWithStats torrent) {
            announceResponseCountDown.countDown();
        }

        @Override
        public void handleDiscoveredPeers(final TorrentWithStats torrent, final List<Peer> peers) {
            discoverPeerCountDown.countDown();
        }
    }

}
