package org.araymond.joal.core.client.emulated;

import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 16/07/2017.
 */
public class BitTorrentClientUrlBuilderTest {

    @Test
    public void shouldBuildReplacePlaceHolders() throws MalformedURLException, UnsupportedEncodingException {
        final Peer peer = Mockito.mock(Peer.class);
        Mockito.when(peer.getPort()).thenReturn(46582);
        Mockito.when(peer.getIp()).thenReturn("123.123.123.123");

        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn(new byte[]{-1, 25, 36, 15});
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);

        final BitTorrentClient client = new BitTorrentClient(
                "myPeerId",
                "mykey",
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&key={key}&event={event}&numwant={numwant}&compact=1&no_peer_id=1&ip={ip}",
                Collections.emptyList(),
                200
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce");
        final URL announceURL = client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, peer);

        assertThat(announceURL.toString()).startsWith(trackerURL.toString());
        assertThat(announceURL.getQuery()).isEqualTo(
                "info_hash=" + URLEncoder.encode(new String(torrent.getTorrent().getInfoHash(), Torrent.BYTE_ENCODING), Torrent.BYTE_ENCODING) +
                        "&peer_id=" + "myPeerId" +
                        "&port=" + peer.getPort() +
                        "&uploaded=" + torrent.getUploaded() +
                        "&downloaded=" + torrent.getDownloaded() +
                        "&left=" + torrent.getLeft() +
                        "&corrupt=0" +
                        "&key=" + "mykey" +
                        "&event=" + RequestEvent.STARTED.getEventName() +
                        "&numwant=" + 200 +
                        "&compact=1" +
                        "&no_peer_id=1" +
                        "&ip=" + peer.getIp()
        );
    }

    @Test
    public void shouldReturnURLEvenIfBaseUrlContainsParams() throws UnsupportedEncodingException, MalformedURLException {
        final Peer peer = Mockito.mock(Peer.class);
        Mockito.when(peer.getPort()).thenReturn(46582);
        Mockito.when(peer.getIp()).thenReturn("123.123.123.123");

        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn(new byte[]{-1, 25, 36, 15});
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);

        final BitTorrentClient client = new BitTorrentClient(
                "myPeerId",
                "mykey",
                "event={event}",
                Collections.emptyList(),
                200
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce?name=jack");
        final URL announceURL = client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, peer);

        assertThat(announceURL.toString()).startsWith(trackerURL.toString());
        assertThat(announceURL.getQuery()).isEqualTo(
                "name=jack&event=" + RequestEvent.STARTED.getEventName()
        );
    }

    @Test
    public void shouldRemoveEventIfEventIsNONE() throws UnsupportedEncodingException, MalformedURLException {
        final Peer peer = Mockito.mock(Peer.class);
        Mockito.when(peer.getPort()).thenReturn(46582);
        Mockito.when(peer.getIp()).thenReturn("123.123.123.123");

        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn(new byte[]{-1, 25, 36, 15});
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);

        final BitTorrentClient client = new BitTorrentClient(
                "myPeerId",
                "mykey",
                "left={left}&event={event}",
                Collections.emptyList(),
                200
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce");
        final URL announceURL = client.buildAnnounceURL(trackerURL, RequestEvent.NONE, torrent, peer);

        assertThat(announceURL.toString()).startsWith(trackerURL.toString());
        assertThat(announceURL.getQuery()).isEqualTo("left=" + torrent.getLeft());
    }

    @Test
    public void shouldFailToBuildIfQueryContainsKeyButBitTorrentClientDoesNot() throws UnsupportedEncodingException, MalformedURLException {
        final Peer peer = Mockito.mock(Peer.class);
        Mockito.when(peer.getPort()).thenReturn(46582);
        Mockito.when(peer.getIp()).thenReturn("123.123.123.123");

        final MockedTorrent subTorrent = Mockito.mock(MockedTorrent.class);
        Mockito.when(subTorrent.getInfoHash()).thenReturn(new byte[]{-1, 25, 36, 15});
        final TorrentWithStats torrent = Mockito.mock(TorrentWithStats.class);
        Mockito.when(torrent.getTorrent()).thenReturn(subTorrent);
        Mockito.when(torrent.getUploaded()).thenReturn(147L);
        Mockito.when(torrent.getDownloaded()).thenReturn(987654L);
        Mockito.when(torrent.getLeft()).thenReturn(0L);

        final BitTorrentClient client = new BitTorrentClient(
                "myPeerId",
                null,
                "key={key}&event={event}",
                Collections.emptyList(),
                200
        );

        final URL trackerURL = new URL("http://my.tracker.com/announce");
        assertThatThrownBy(() -> client.buildAnnounceURL(trackerURL, RequestEvent.STARTED, torrent, peer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Client request query contains 'key' but BitTorrentClient does not have a key.");
    }

}
