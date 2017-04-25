package org.araymond.joal.core.ttorent.common.protocol.http;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 05/04/2017.
 */
public class HTTPAnnounceRequestMessageTest {

    private BitTorrentClient createEmulatedClientForQuery(final String query) {
        return new BitTorrentClient(
                "A2-1-30-0-qdsqdsqz",
                "1q5d4z9",
                query,
                new ArrayList<>(),
                200
        );
    }

    private HTTPAnnounceRequestMessage createDefaultAnnounceMessageMock() {
        final HTTPAnnounceRequestMessage announceMsg = Mockito.mock(HTTPAnnounceRequestMessage.class);
        Mockito.when(announceMsg.getInfoHash()).thenReturn("thisIsInfoHash".getBytes());
        Mockito.when(announceMsg.getPeerId()).thenReturn("thisIsPeerID".getBytes());
        Mockito.when(announceMsg.getPort()).thenReturn(3000);
        Mockito.when(announceMsg.getUploaded()).thenReturn(546498465L);
        Mockito.when(announceMsg.getDownloaded()).thenReturn(0L);
        Mockito.when(announceMsg.getLeft()).thenReturn(0L);
        Mockito.when(announceMsg.getEvent()).thenReturn(RequestEvent.STARTED);
        Mockito.when(announceMsg.getIp()).thenReturn("192.168.1.2");

        return announceMsg;
    }

    @Test
    public void shouldBuildAnnounceURL() throws MalformedURLException, UnsupportedEncodingException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessageMock();
        Mockito.when(announceMsg.buildAnnounceURL(Mockito.any(), Mockito.any())).thenCallRealMethod();


        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .startsWith(baseUrl.toString())
                .contains("info_hash=" + new String(announceMsg.getInfoHash()))
                .contains("peer_id=" + new String(announceMsg.getPeerId()))
                .contains("port=" + announceMsg.getPort())
                .contains("uploaded=" + 546498465L)
                .contains("downloaded=" + 0L)
                .contains("left=" + 0L)
                .contains("event=" + RequestEvent.STARTED.getEventName())
                .contains("numwant=" + client.getNumwant())
                .contains("key=" + client.getKey().get())
                .contains("ip=" + announceMsg.getIp());
    }

    @Test
    public void shouldBuildCorrectlyEvenIfBaseUrlContainsParams() throws MalformedURLException, UnsupportedEncodingException {
        final URL baseUrl = new URL("http://localhost?name=jack");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessageMock();
        Mockito.when(announceMsg.buildAnnounceURL(Mockito.any(), Mockito.any())).thenCallRealMethod();

        try {
            assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                    .isEqualTo(
                            baseUrl.toString() + "&info_hash=" + new String(announceMsg.getInfoHash())
                    );
        } catch (final MalformedURLException ignored) {
            fail("Failed to build announce request if base url already contains '?'");
        }
    }

    @Test
    public void shouldBuildAnnounceURLAnStayOrderedAsQuery() throws MalformedURLException, UnsupportedEncodingException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessageMock();
        Mockito.when(announceMsg.buildAnnounceURL(Mockito.any(), Mockito.any())).thenCallRealMethod();

        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .isEqualTo(
                        baseUrl.toString()
                        + "?" + "info_hash=" + new String(announceMsg.getInfoHash())
                        + "&" + "peer_id=" + new String(announceMsg.getPeerId())
                        + "&" + "port=" + announceMsg.getPort()
                        + "&" + "uploaded=" + 546498465L
                        + "&" + "downloaded=" + 0L
                        + "&" + "left=" + 0L
                        + "&" + "event=" + RequestEvent.STARTED.getEventName()
                        + "&" + "numwant=" + client.getNumwant()
                        + "&" + "key=" + client.getKey().get()
                        + "&" + "ip=" + announceMsg.getIp()
                );
    }

    @Test
    public void shouldRemoveEventIfEventIsNONE() throws MalformedURLException, UnsupportedEncodingException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessageMock();
        Mockito.when(announceMsg.buildAnnounceURL(Mockito.any(), Mockito.any())).thenCallRealMethod();
        Mockito.when(announceMsg.getEvent()).thenReturn(RequestEvent.NONE);

        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .doesNotContain("event");
    }

}
