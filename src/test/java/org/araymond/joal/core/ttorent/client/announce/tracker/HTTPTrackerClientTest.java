package org.araymond.joal.core.ttorent.client.announce.tracker;

import com.google.common.collect.Lists;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.http.HTTPTrackerMessage;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by raymo on 05/06/2017.
 */
public class HTTPTrackerClientTest {

    @Test
    public void shouldBuild() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient client = Mockito.mock(BitTorrentClient.class);

        try {
            new HTTPTrackerClient(torrent, connectionHandler, uri, client);
        } catch (final Throwable t) {
            fail("Should have built.", t);
        }
    }

    @Test
    public void shouldNotBuildWithoutBitTorrentClient() throws URISyntaxException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient client = null;

        assertThatThrownBy(() -> new HTTPTrackerClient(torrent, connectionHandler, uri, client))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("BitTorrentClient must not be null.");
    }

    @Test
    public void shouldAddHeaders() throws URISyntaxException {
        final List<AbstractMap.Entry<String, String>> headers = Lists.newArrayList(
                new AbstractMap.SimpleEntry<>("myKey", "myValue"),
                new AbstractMap.SimpleEntry<>("mySecondKey", "mySecondValue")
        );
        final BitTorrentClient bitTorrentClient = Mockito.mock(BitTorrentClient.class);
        Mockito.when(bitTorrentClient.getHeaders()).thenReturn(headers);


        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.doNothing().when(connection).addRequestProperty(Mockito.anyString(), Mockito.anyString());

        new HTTPTrackerClient(torrent, connectionHandler, uri, bitTorrentClient).addHttpHeaders(connection);

        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("myKey", "myValue");
        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("mySecondKey", "mySecondValue");
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void shouldReplacePlaceholderInHeaders() throws URISyntaxException {

        final List<AbstractMap.Entry<String, String>> headers = Lists.newArrayList(
                new AbstractMap.SimpleEntry<>("javaOnly", "{java}"),
                new AbstractMap.SimpleEntry<>("osOnly", "{os}"),
                new AbstractMap.SimpleEntry<>("javaWithText", "Java v{java} qsdqd"),
                new AbstractMap.SimpleEntry<>("osWithText", "Os {os} qdqsdqsd")
        );
        final BitTorrentClient bitTorrentClient = Mockito.mock(BitTorrentClient.class);
        Mockito.when(bitTorrentClient.getHeaders()).thenReturn(headers);

        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");

        final HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.doNothing().when(connection).addRequestProperty(Mockito.anyString(), Mockito.anyString());

        new HTTPTrackerClient(torrent, connectionHandler, uri, bitTorrentClient).addHttpHeaders(connection);

        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("javaOnly", System.getProperty("java.version"));
        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("osOnly", System.getProperty("os.name"));
        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("javaWithText", "Java v" + System.getProperty("java.version") + " qsdqd");
        Mockito.verify(connection, Mockito.times(1)).addRequestProperty("osWithText", "Os " + System.getProperty("os.name") + " qdqsdqsd");
        Mockito.verifyNoMoreInteractions(connection);
    }

    @Test
    public void shouldTransformByteBufferToTrackerMessage() throws AnnounceException, URISyntaxException, IOException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient bitTorrentClient = Mockito.mock(BitTorrentClient.class);

        final HTTPTrackerMessage msg = new HTTPTrackerClient(torrent, connectionHandler, uri, bitTorrentClient).toTrackerMessage(createValidHttpAnnounceResponseAsByteBuffer());

        assertThat(msg.getType()).isEqualTo(TrackerMessage.Type.ANNOUNCE_RESPONSE);

        final TrackerMessage.AnnounceResponseMessage announceMsg = (TrackerMessage.AnnounceResponseMessage) msg;
        assertThat(announceMsg.getIncomplete()).isEqualTo(100);
        assertThat(announceMsg.getComplete()).isEqualTo(2945);
        assertThat(announceMsg.getInterval()).isEqualTo(1800);
        assertThat(announceMsg.getPeers()).hasSize(1);
        assertThat(announceMsg.getPeers().get(0).toString()).isEqualTo(new Peer("8.8.8.8", 2048).toString());
    }

    @Test
    public void shouldFailTransformByteBufferToTrackerMessageIfResponseIsMissFormatted() throws AnnounceException, URISyntaxException, IOException {
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        final URI uri = new URI("http://example.tracker.com/announce");
        final BitTorrentClient bitTorrentClient = Mockito.mock(BitTorrentClient.class);

        final ByteBuffer buffer = createValidHttpAnnounceResponseAsByteBuffer().put(0, (byte) 50);
        assertThatThrownBy(() -> new HTTPTrackerClient(torrent, connectionHandler, uri, bitTorrentClient).toTrackerMessage(buffer))
                .isInstanceOf(AnnounceException.class)
                .hasMessage("Error reading tracker response!");
    }

    public ByteBuffer createValidHttpAnnounceResponseAsByteBuffer() throws IOException {
        final Map<String, BEValue> map = new HashMap<>();

        map.put("incomplete", new BEValue(100));
        map.put("interval", new BEValue(1800));
        map.put("complete", new BEValue(2945));
        map.put("peers", new BEValue(new byte[]{8, 8, 8, 8, 8, 0}));

        return BEncoder.bencode(map);
    }

}
