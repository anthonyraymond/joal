package org.araymond.joal.core.client.emulated;

import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.key.KeyGeneratorTest;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGeneratorTest;
import org.araymond.joal.core.exception.UnrecognizedAnnounceParameter;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
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
    private static final NumwantProvider defaultNumwantProvider = new NumwantProvider(200, 0);

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

    private static final TorrentWithStats createMockedTorrentWithStats() {
        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn("af2d3c294faf2d3c294faf2d3c294f".getBytes());
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);
        return torrent;
    }

    @Test
    public void shouldFailIfPlaceHoldersRemainsInURL() {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "info_hash={infohash}&what_is_that={damnit}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThatThrownBy(() ->
                client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler)
        ).isInstanceOf(UnrecognizedAnnounceParameter.class)
                .hasMessage("Placeholder {damnit} were not recognized while building announce URL.");
    }

    @Test
    public void shouldReplaceInfoHash() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "info_hash={infohash}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("info_hash=" + URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING));
    }

    @Test
    public void shouldReplacePeerId() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "peer_id={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("peer_id=" + defaultPeerIdGenerator.getPeerId(torrent.getTorrent(), RequestEvent.STARTED));
    }

    @Test
    public void shouldReplaceUploaded() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "uploaded={uploaded}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("uploaded=" + torrent.getUploaded());
    }

    @Test
    public void shouldReplaceDownloaded() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "downloaded={downloaded}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("downloaded=" + torrent.getDownloaded());
    }

    @Test
    public void shouldReplaceLeft() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "left={left}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("left=" + torrent.getLeft());
    }

    @Test
    public void shouldReplacePort() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "port={port}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("port=" + connHandler.getPort());
    }

    @Test
    public void shouldReplaceIpv6AndRemoveIpv4() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet6Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "ipv6={ipv6}&ip={ip}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("ipv6=" + URLEncoder.encode(connHandler.getIpAddress().getHostAddress(), Torrent.BYTE_ENCODING));
    }

    @Test
    public void shouldReplaceIpv4AndRemoveIpv6() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "ip={ip}&ipv6={ipv6}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("ip=" + connHandler.getIpAddress().getHostAddress());
    }

    @Test
    public void shouldReplaceEvent() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("event=" + RequestEvent.STARTED.getEventName());
    }

    @Test
    public void shouldRemoveEventIfNone() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.NONE, torrent, connHandler).getQuery())
                .isEqualTo("");
    }

    @Test
    public void shouldReplaceKey() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "key={key}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("key=" + defaultKeyGenerator.getKey(torrent.getTorrent(), RequestEvent.STARTED));
    }

    @Test
    public void shouldReplaceNumWant() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "numwant={numwant}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("numwant=" + defaultNumwantProvider.get(RequestEvent.STARTED));
    }

    @Test
    public void shouldReplaceNumWantWithNumwantOnStopValue() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "numwant={numwant}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STOPPED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .isEqualTo("numwant=" + defaultNumwantProvider.get(RequestEvent.STOPPED));
    }

    @Test
    public void shouldBuildReplaceMultipleValues() throws MalformedURLException, UnsupportedEncodingException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "info_hash={infohash}&downloaded={downloaded}&left={left}&corrupt=0&ip={ip}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce");
        final URL announceURL = client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.toString()).startsWith(trackerURL.toString());
        assertThat(announceURL.getQuery()).isEqualTo(
                "info_hash=" + URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING) +
                        "&downloaded=" + torrent.getDownloaded() +
                        "&left=" + torrent.getLeft() +
                        "&corrupt=0" +
                        "&ip=" + connHandler.getIpAddress().getHostAddress()
        );
    }

    @Test
    public void shouldReturnURLEvenIfBaseUrlContainsParams() throws UnsupportedEncodingException, MalformedURLException {
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce?name=jack");
        final URL announceURL = client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.toString()).startsWith(trackerURL.toString());
        assertThat(announceURL.getQuery()).isEqualTo(
                "name=jack&event=" + RequestEvent.STARTED.getEventName()
        );
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
                "key={key}&event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce");
        assertThatThrownBy(() -> client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, connectionHandler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Client request query contains 'key' but BitTorrentClient does not have a key.");
    }

    @Test
    public void shouldURLEncodeUnicodeCharactersInPeerId() throws MalformedURLException, UnsupportedEncodingException {
        final PeerIdGenerator peerIdGeneratorWithUnicodePrefix = PeerIdGeneratorTest.createDefault("-AA-" + (char) 0x0089 + (char) 0x00F9);
        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet4Address());
        final TorrentWithStats torrent = createMockedTorrentWithStats();
        final BitTorrentClient client = new BitTorrentClient(
                peerIdGeneratorWithUnicodePrefix,
                defaultKeyGenerator,
                "peer_id={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );


        final URL announceURL = client.buildAnnounceURL(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);

        assertThat(announceURL.getQuery())
                .startsWith("peer_id=-AA-%89%F9");
    }

}
