package org.araymond.joal.core.ttorent.common.protocol.http;

import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static com.turn.ttorrent.bcodec.BDecoder.bdecode;
import static org.assertj.core.api.Assertions.*;

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

    private HTTPAnnounceRequestMessage createDefaultAnnounceMessage(final BitTorrentClient client) throws IOException, TrackerMessage.MessageValidationException {
        final byte[] infoHash = "thisIsInfoHash".getBytes(Charsets.ISO_8859_1);
        final byte[] peerId = "peerId".getBytes(Charsets.ISO_8859_1);
        final Integer port = 6845;
        final Long uploaded = 150L;
        final Long downloaded = 10L;
        final Long left = 90L;
        final boolean compact = false;
        final boolean noPeerId = true;
        final RequestEvent event = RequestEvent.STARTED;
        final String ip = "192.168.1.50";

        return HTTPAnnounceRequestMessage.craft(infoHash, peerId, port, uploaded, downloaded, left, compact, noPeerId, event, ip, client);
    }

    @Test
    public void shouldBuildAnnounceURL() throws IOException, TrackerMessage.MessageValidationException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessage(client);

        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .startsWith(baseUrl.toString())
                .contains("info_hash=" + new String(announceMsg.getInfoHash(), Charsets.ISO_8859_1))
                .contains("peer_id=" + new String(announceMsg.getPeerId(), Charsets.ISO_8859_1))
                .contains("port=" + announceMsg.getPort())
                .contains("uploaded=" + announceMsg.getUploaded())
                .contains("downloaded=" + announceMsg.getDownloaded())
                .contains("left=" + announceMsg.getLeft())
                .contains("event=" + announceMsg.getEvent().getEventName())
                .contains("numwant=" + client.getNumwant())
                .contains("key=" + client.getKey().orElseThrow(() -> new AssertionError("Should have a key")))
                .contains("ip=" + announceMsg.getIp())
        ;
    }

    @Test
    public void shouldBuildCorrectlyEvenIfBaseUrlContainsParams() throws IOException, TrackerMessage.MessageValidationException {
        final URL baseUrl = new URL("http://localhost?name=jack");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessage(client);

        try {
            assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                    .isEqualTo(
                            baseUrl.toString() + "&info_hash=" + new String(announceMsg.getInfoHash(), Charsets.ISO_8859_1)
                    );
        } catch (final MalformedURLException ignored) {
            fail("Failed to build announce request if base url already contains '?'");
        }
    }

    @Test
    public void shouldBuildAnnounceURLAnStayOrderedAsQuery() throws IOException, TrackerMessage.MessageValidationException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = createDefaultAnnounceMessage(client);

        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .isEqualTo(
                        baseUrl.toString()
                                + "?" + "info_hash=" + new String(announceMsg.getInfoHash(), Charsets.ISO_8859_1)
                                + "&" + "peer_id=" + new String(announceMsg.getPeerId(), Charsets.ISO_8859_1)
                                + "&" + "port=" + announceMsg.getPort()
                                + "&" + "uploaded=" + announceMsg.getUploaded()
                                + "&" + "downloaded=" + announceMsg.getDownloaded()
                                + "&" + "left=" + announceMsg.getLeft()
                                + "&" + "event=" + announceMsg.getEvent().getEventName()
                                + "&" + "numwant=" + client.getNumwant()
                                + "&" + "key=" + client.getKey().orElseThrow(() -> new AssertionError("Should have a key"))
                                + "&" + "ip=" + announceMsg.getIp()
                );
    }

    @Test
    public void shouldRemoveEventIfEventIsNONE() throws IOException, TrackerMessage.MessageValidationException {
        final URL baseUrl = new URL("http://localhost");
        final BitTorrentClient client = createEmulatedClientForQuery(
                "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}"
        );

        final HTTPAnnounceRequestMessage announceMsg = Mockito.spy(createDefaultAnnounceMessage(client));
        Mockito.when(announceMsg.getEvent()).thenReturn(RequestEvent.NONE);

        assertThat(announceMsg.buildAnnounceURL(baseUrl, client).toString())
                .doesNotContain("event");
    }

    @Test
    public void shouldFailToBuildIfQueryContainsKeyButBitTorrentClientDoesNot() throws IOException, TrackerMessage.MessageValidationException {
        final URL baseUrl = new URL("http://localhost");
        final String query = "info_hash={infohash}&peer_id={peerid}&port={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&event={event}&numwant={numwant}&key={key}&ip={ip}";
        final BitTorrentClient client = new BitTorrentClient(
                "A2-1-30-0-qdsqdsqz",
                null,
                query,
                new ArrayList<>(),
                200
        );

        final HTTPAnnounceRequestMessage announceMessage = createDefaultAnnounceMessage(client);

        assertThatThrownBy(() -> announceMessage.buildAnnounceURL(baseUrl, client))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Client request query contains 'key' but BitTorrentClient does not have a key.");
    }

    @Test
    public void shouldCraft() throws IOException, TrackerMessage.MessageValidationException {
        final BitTorrentClient client = createEmulatedClientForQuery("q");

        final byte[] infoHash = "thisIsInfoHash".getBytes(Charsets.ISO_8859_1);
        final byte[] peerId = "peerId".getBytes(Charsets.ISO_8859_1);
        final Integer port = 6845;
        final Long uploaded = 150L;
        final Long downloaded = 10L;
        final Long left = 90L;
        final boolean compact = false;
        final boolean noPeerId = true;
        final RequestEvent event = RequestEvent.STARTED;
        final String ip = "192.168.1.50";

        final HTTPAnnounceRequestMessage message = HTTPAnnounceRequestMessage.craft(infoHash, peerId, port, uploaded, downloaded, left, compact, noPeerId, event, ip, client);

        assertThat(message.getInfoHash()).isEqualTo(infoHash);
        assertThat(message.getPeerId()).isEqualTo(peerId);
        assertThat(message.getPort()).isEqualTo(port);
        assertThat(message.getUploaded()).isEqualTo(uploaded);
        assertThat(message.getDownloaded()).isEqualTo(downloaded);
        assertThat(message.getLeft()).isEqualTo(left);
        assertThat(message.getCompact()).isEqualTo(compact);
        assertThat(message.getNoPeerIds()).isEqualTo(noPeerId);
        assertThat(message.getNumWant()).isEqualTo(client.getNumwant());
        assertThat(message.getEvent()).isEqualTo(event);
        assertThat(message.getIp()).isEqualTo(ip);

        assertThat(message.getHexInfoHash()).isEqualTo(Torrent.byteArrayToHexString(infoHash));
        assertThat(message.getHexPeerId()).isEqualTo(Torrent.byteArrayToHexString(peerId));

        assertThat(bdecode(message.getData()).getMap())
                .hasSize(11)
                // comparing keys
                .containsKey("info_hash")
                .containsKey("peer_id")
                .containsKey("port")
                .containsKey("uploaded")
                .containsKey("downloaded")
                .containsKey("left")
                .containsKey("compact")
                .containsKey("no_peer_id")
                .containsKey("numwant")
                .containsKey("ip")
                .containsKey("event")
                .has(new Condition<>(map -> {
                    try {
                        return Arrays.equals(map.get("info_hash").getBytes(), infoHash);
                    } catch (final InvalidBEncodingException e) {
                        return false;
                    }
                }, "info_hash key should have value: " + new String(infoHash, Charsets.ISO_8859_1)))
                .has(new Condition<>(map -> {
                    try {
                        return Arrays.equals(map.get("peer_id").getBytes(), peerId);
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "peer_id key should have value:" + new String(peerId, Charsets.ISO_8859_1)))
                .has(new Condition<>(map -> {
                    try {
                        return Ints.compare(map.get("port").getInt(), port) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "port key should have value:" + port))
                .has(new Condition<>(map -> {
                    try {
                        return Longs.compare(map.get("uploaded").getLong(), uploaded) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "uploaded key should have value:" + uploaded))
                .has(new Condition<>(map -> {
                    try {
                        return Longs.compare(map.get("downloaded").getLong(), downloaded) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "downloaded key should have value:" + downloaded))
                .has(new Condition<>(map -> {
                    try {
                        return Longs.compare(map.get("left").getLong(), left) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "left key should have value:" + left))
                .has(new Condition<>(map -> {
                    try {
                        //noinspection ConstantConditions
                        return Ints.compare(map.get("compact").getInt(), compact ? 1 : 0) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "compact key should have value:" + (compact ? 1 : 0)))
                .has(new Condition<>(map -> {
                    try {
                        //noinspection ConstantConditions
                        return Ints.compare(map.get("no_peer_id").getInt(), noPeerId ? 1 : 0) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "no_peer_id key should have value:" + (noPeerId ? 1 : 0)))
                .has(new Condition<>(map -> {
                    try {
                        return Ints.compare(map.get("numwant").getInt(), client.getNumwant()) == 0;
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "numwant key should have value:" + client.getNumwant()))
                .has(new Condition<>(map -> {
                    try {
                        return map.get("ip").getString().equals(ip);
                    } catch (InvalidBEncodingException e) {
                        return false;
                    }
                }, "ip key should have value:" + ip))
                .has(new Condition<>(map -> {
                    try {
                        return map.get("event").getString().equals(event.getEventName());
                    } catch (final InvalidBEncodingException e) {
                        return false;
                    }
                }, "event key should have value:" + event.getEventName()));
    }

}
