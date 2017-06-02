package org.araymond.joal.core.ttorent.client.announce;

import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import com.turn.ttorrent.common.protocol.http.HTTPAnnounceRequestMessage;
import com.turn.ttorrent.common.protocol.http.HTTPAnnounceResponseMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerErrorMessage;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 02/06/2017.
 */
public class TrackerClientTest {

    @Test
    public void shouldNotBuildWithoutTorrent() throws URISyntaxException {
        final TorrentWithStats torrent = null;
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = new URI("http://example.tracker.com/announce");

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, peer, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Torrent must not be null.");
    }

    @Test
    public void shouldNotBuildWithoutPeer() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = null;
        final URI uri = new URI("http://example.tracker.com/announce");

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, peer, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Peer must not be null.");
    }

    @Test
    public void shouldNotBuildWithoutURI() {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = null;

        assertThatThrownBy(() -> new DefaultTrackerClient(torrent, peer, uri))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("URI must not be null.");
    }

    @Test
    public void shouldBuild() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, peer, uri);

        assertThat(trackerClient.getTorrentWithStats()).isEqualTo(torrent);
        assertThat(trackerClient.getPeer()).isEqualTo(peer);
        assertThat(trackerClient.getTrackerURI()).isEqualTo(uri);
    }

    @Test
    public void shouldThrowAnnounceExceptionWhenMessageIsAnErrorMessageOnHandleTrackerResponse() throws URISyntaxException, IOException, TrackerMessage.MessageValidationException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, peer, uri);

        final TrackerMessage.ErrorMessage message = HTTPTrackerErrorMessage.craft(TrackerMessage.ErrorMessage.FailureReason.UNKNOWN_TORRENT.getMessage());

        assertThatThrownBy(() -> trackerClient.handleTrackerAnnounceResponse((TrackerMessage) message))
                .isInstanceOf(AnnounceException.class)
                .hasMessage(TrackerMessage.ErrorMessage.FailureReason.UNKNOWN_TORRENT.getMessage());
    }

    @Test
    public void shouldThrowAnnounceExceptionWhenMessageIsNotAnErrorOrResponseMessageOnHandleTrackerResponse() throws URISyntaxException, IOException, TrackerMessage.MessageValidationException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, peer, uri);

        final TrackerMessage message = Mockito.mock(HTTPAnnounceRequestMessage.class);
        Mockito.when(message.getType()).thenReturn(TrackerMessage.Type.ANNOUNCE_REQUEST);

        assertThatThrownBy(() -> trackerClient.handleTrackerAnnounceResponse(message))
                .isInstanceOf(AnnounceException.class)
                .hasMessage("Unexpected tracker message type ANNOUNCE_REQUEST!");
    }

    @Test
    public void shouldNotifyListenerOnHandleTrackerResponse() throws URISyntaxException, AnnounceException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final Peer peer = new Peer(new InetSocketAddress("127.0.0.1", ConnectionHandler.PORT_RANGE_START));
        final URI uri = new URI("http://example.tracker.com/announce");

        final DefaultTrackerClient trackerClient = new DefaultTrackerClient(torrent, peer, uri);
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
        Mockito.verify(listener, Mockito.times(1)).handleAnnounceResponse(
                torrent,
                message.getInterval(),
                message.getComplete(),
                message.getIncomplete()
        );
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

        DefaultTrackerClient(final TorrentWithStats torrent, final Peer peer, final URI tracker) {
            super(torrent, peer, tracker);
        }

        @Override
        public void announce(final RequestEvent event) throws AnnounceException {
        }

        TorrentWithStats getTorrentWithStats() {
            return this.torrent;
        }
        Peer getPeer() {
            return this.peer;
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
        public void handleAnnounceResponse(final TorrentWithStats torrent, final int interval, final int complete, final int incomplete) {
            announceResponseCountDown.countDown();
        }

        @Override
        public void handleDiscoveredPeers(final TorrentWithStats torrent, final List<Peer> peers) {
            discoverPeerCountDown.countDown();
        }
    }

}
