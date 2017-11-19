package org.araymond.joal.core.client.emulated;

import com.google.common.collect.Lists;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.http.client.fluent.Request;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.key.KeyGeneratorTest;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGeneratorTest;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.exception.UnrecognizedAnnounceParameter;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStatsTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class BitTorrentClientUrlBuilderTest {
    private static final KeyGenerator defaultKeyGenerator = KeyGeneratorTest.createDefault();
    private static final PeerIdGenerator defaultPeerIdGenerator = PeerIdGeneratorTest.createDefault();
    private final UrlEncoder defaultUrlEncoder = new UrlEncoder(".*", Casing.LOWER);
    private static final NumwantProvider defaultNumwantProvider = new NumwantProvider(200, 0);
    private static URL defaultTrackerURL;

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        defaultTrackerURL = new URL("http://my.tracker.com/announce");
    }

    private static String computeExpectedURLBegin(final URL trackerURL) {
        return "GET " + trackerURL.toString() + "?";
    }

    private static InetAddress createMockedINet4Address() {
        try {
            return InetAddress.getByName("123.123.123.123");
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static InetAddress createMockedINet6Address() {
        try {
            return InetAddress.getByName("fd2d:7212:4cd5:2f14:ffff:ffff:ffff:ffff");
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ConnectionHandler createMockedConnectionHandler(final InetAddress inetAddress) {
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        Mockito.when(connectionHandler.getPort()).thenReturn(46582);
        Mockito.when(connectionHandler.getIpAddress()).thenReturn(inetAddress);
        return connectionHandler;
    }

    @Test
    public void shouldFailIfPlaceHoldersRemainsInURL() {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "info_hash={infohash}&what_is_that={damnit}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThatThrownBy(() ->
                client.buildAnnounceRequest(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler)
        ).isInstanceOf(UnrecognizedAnnounceParameter.class)
                .hasMessage("Placeholder {damnit} were not recognized while building announce URL.");
    }

    @Test
    public void shouldReplaceInfoHash() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "info_hash={infohash}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "info_hash=" + defaultUrlEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING)) +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldUrlEncodeInfoHash() throws UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                new UrlEncoder("", Casing.LOWER),
                "info_hash={infohash}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .contains("%");
    }

    @Test
    public void shouldReplacePeerId() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "peer_id={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "peer_id=" + defaultPeerIdGenerator.getPeerId(torrent.getTorrent(), RequestEvent.STARTED) +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldUrlEncodePeerId() throws UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                PeerIdGeneratorTest.createForPattern("-AA-[a]{16}", true),
                defaultKeyGenerator,
                new UrlEncoder("", Casing.LOWER),
                "peer_id={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .contains("%61%61%61%61%61%61%61%61%61%61%61%61%61%61%61%61");
    }

    @Test
    public void shouldNotUrlEncodePeerId() throws UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                PeerIdGeneratorTest.createForPattern("-AA-[a]{16}", false),
                defaultKeyGenerator,
                new UrlEncoder("", Casing.LOWER),
                "peer_id={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .contains("aaaaaaaaaaaaaaaa");
    }

    @Test
    public void shouldReplaceUploaded() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "uploaded={uploaded}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "uploaded=" + torrent.getUploaded() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceDownloaded() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "downloaded={downloaded}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "downloaded=" + torrent.getDownloaded() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceLeft() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "left={left}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "left=" + torrent.getLeft() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplacePort() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "port={port}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "port=" + connHandler.getPort() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceIpv6AndRemoveIpv4() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet6Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                new UrlEncoder("[0-9a-zA-Z]", Casing.LOWER),
                "ipv6={ipv6}&ip={ip}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(computeExpectedURLBegin(defaultTrackerURL) +
                        "ipv6=" + connHandler.getIpAddress().getHostAddress().replaceAll(":", "%3a")
                        + " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceIpv4AndRemoveIpv6() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "ip={ip}&ipv6={ipv6}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "ip=" + connHandler.getIpAddress().getHostAddress() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceEvent() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "event=" + RequestEvent.STARTED.getEventName() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldRemoveEventIfNone() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.NONE, torrent, connHandler).toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL).replaceAll("\\?", "") +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceKey() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "key={key}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "key=" + defaultKeyGenerator.getKey(torrent.getTorrent(), RequestEvent.STARTED) +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceNumWant() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "numwant={numwant}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "numwant=" + defaultNumwantProvider.get(RequestEvent.STARTED) +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReplaceNumWantWithNumwantOnStopValue() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "numwant={numwant}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STOPPED, torrent, connHandler);

        assertThat(request.toString())
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "numwant=" + defaultNumwantProvider.get(RequestEvent.STOPPED) +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldBuildReplaceMultipleValues() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "info_hash={infohash}&downloaded={downloaded}&left={left}&corrupt=0&ip={ip}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final Request request = client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString())
                .startsWith(computeExpectedURLBegin(defaultTrackerURL))
                .isEqualTo(
                        computeExpectedURLBegin(defaultTrackerURL) +
                                "info_hash=" + defaultUrlEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING)) +
                                "&downloaded=" + torrent.getDownloaded() +
                                "&left=" + torrent.getLeft() +
                                "&corrupt=0" +
                                "&ip=" + connHandler.getIpAddress().getHostAddress() +
                                " HTTP/1.1"
                );
    }

    @Test
    public void shouldReturnURLEvenIfBaseUrlContainsParams() throws UnsupportedEncodingException, MalformedURLException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce?name=jack");
        final Request request = client.buildAnnounceRequest(trackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(request.toString()).startsWith("GET " + trackerURL.toString())
                .isEqualTo(
                        computeExpectedURLBegin(trackerURL).replaceAll("\\?$", "") +
                                "&event=" + RequestEvent.STARTED.getEventName() +
                                " HTTP/1.1"
                ).contains("?name=jack");
    }

    @Test
    public void shouldFailToBuildIfQueryContainsKeyButBitTorrentClientDoesNot() throws UnsupportedEncodingException, MalformedURLException {
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        Mockito.when(connectionHandler.getPort()).thenReturn(46582);
        Mockito.when(connectionHandler.getIpAddress()).thenReturn(createMockedINet4Address());

        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn(new byte[]{-1, 25, 36, 15});
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                null,
                defaultUrlEncoder,
                "key={key}&event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        assertThatThrownBy(() -> client.buildAnnounceRequest(defaultTrackerURL, RequestEvent.STARTED, torrent, connectionHandler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Client request query contains 'key' but BitTorrentClient does not have a key.");
    }

    @Test
    public void shouldAddHeaders() {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(new HttpHeader("HName", "HValue"), new HttpHeader("Accept", "*/*")),
                defaultNumwantProvider
        );

        final Request request = Mockito.mock(Request.class);

        client.addHeadersToRequest(request, defaultTrackerURL);
        Mockito.verify(request, Mockito.times(1)).addHeader("HName", "HValue");
        Mockito.verify(request, Mockito.times(1)).addHeader("Accept", "*/*");
    }

    @Test
    public void shouldAddConnectionHeaderIfNotPresent() {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(),
                defaultNumwantProvider
        );

        final Request request = Mockito.mock(Request.class);

        client.addHeadersToRequest(request, defaultTrackerURL);
        Mockito.verify(request, Mockito.times(1)).addHeader("Connection", "Close");
    }

    @Test
    public void shouldNotOverrideHeaderIfPresent() {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(new HttpHeader("Connection", "WOAW")),
                defaultNumwantProvider
        );

        final Request request = Mockito.mock(Request.class);

        client.addHeadersToRequest(request, defaultTrackerURL);
        Mockito.verify(request, Mockito.times(1)).addHeader("Connection", "WOAW");
    }

    @Test
    public void shouldAddHostHeader() throws MalformedURLException {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(new HttpHeader("Connection", "WOAW")),
                defaultNumwantProvider
        );

        final Request requestWithoutPort = Mockito.mock(Request.class);

        final URL urlWithoutPort = new URL("http://my.tracker.com");
        client.addHeadersToRequest(requestWithoutPort, urlWithoutPort);
        Mockito.verify(requestWithoutPort, Mockito.times(1)).addHeader("Host", urlWithoutPort.getHost());

        final Request requestWithPort = Mockito.mock(Request.class);

        final URL urlWithPort = new URL("http://my.tracker.com:1234");
        client.addHeadersToRequest(requestWithPort, urlWithPort);
        Mockito.verify(requestWithPort, Mockito.times(1)).addHeader("Host", urlWithPort.getHost() + ":" + urlWithPort.getPort());

    }

    @Test
    public void shouldAddHeadersInOrder() {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(
                        new HttpHeader("User-Agent", "jacki-jack"),
                        new HttpHeader("Connection", "Close"),
                        new HttpHeader("Accept", "*/*"),
                        new HttpHeader("Accept-Encoding", "gzip"),
                        new HttpHeader("Another-Name", "Another-Value")
                ),
                defaultNumwantProvider
        );

        final Request request = Mockito.mock(Request.class);
        client.addHeadersToRequest(request, defaultTrackerURL);

        final InOrder inOrder = Mockito.inOrder(request);
        inOrder.verify(request, Mockito.times(1)).addHeader("Host", defaultTrackerURL.getHost());
        Mockito.verify(request, Mockito.times(1)).addHeader("User-Agent", "jacki-jack");
        Mockito.verify(request, Mockito.times(1)).addHeader("Connection", "Close");
        Mockito.verify(request, Mockito.times(1)).addHeader("Accept", "*/*");
        Mockito.verify(request, Mockito.times(1)).addHeader("Accept-Encoding", "gzip");
        Mockito.verify(request, Mockito.times(1)).addHeader("Another-Name", "Another-Value");
    }

    @Test
    public void shouldReplacePlaceholderInHeaders() throws URISyntaxException {
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "d",
                Lists.newArrayList(
                        new HttpHeader("javaOnly", "{java}"),
                        new HttpHeader("osOnly", "{os}"),
                        new HttpHeader("javaWithText", "Java v{java} qsdqd"),
                        new HttpHeader("osWithText", "Os {os} qdqsdqsd")
                ),
                defaultNumwantProvider
        );

        final Request request = Mockito.mock(Request.class);
        client.addHeadersToRequest(request, defaultTrackerURL);

        Mockito.verify(request, Mockito.times(1)).addHeader("javaOnly", System.getProperty("java.version"));
        Mockito.verify(request, Mockito.times(1)).addHeader("osOnly", System.getProperty("os.name"));
        Mockito.verify(request, Mockito.times(1)).addHeader("javaWithText", "Java v" + System.getProperty("java.version") + " qsdqd");
        Mockito.verify(request, Mockito.times(1)).addHeader("osWithText", "Os " + System.getProperty("os.name") + " qdqsdqsd");
    }

}
