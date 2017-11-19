package org.araymond.joal.core.client.emulated;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.key.KeyGeneratorTest;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGeneratorTest;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class BitTorrentClientConfigTest {
    private final PeerIdGenerator defaultPeerIdGenerator = PeerIdGeneratorTest.createDefault();
    private final UrlEncoder defaultUrlEncoder = new UrlEncoder(".*", Casing.LOWER);

    @Test
    public void shouldBuildEvenIfKeyGeneratorIsNotDefined() {
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(defaultPeerIdGenerator, query, null, defaultUrlEncoder, requestHeaders, 200, 0);

        assertThat(config.createClient().getKey(Mockito.mock(MockedTorrent.class), RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldNotBuildIfKeyGeneratorIsNotDefinedButKeyIsInQueryUrl() {
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        assertThatThrownBy(() -> new BitTorrentClientConfig(defaultPeerIdGenerator, query, null, defaultUrlEncoder, requestHeaders, 200, 0))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessageContaining("Query string contains {key}, but no keyGenerator was found in .client file.");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(PeerIdGeneratorTest.createDefault(), query, KeyGeneratorTest.createDefault(), defaultUrlEncoder, requestHeaders, 200, 0);
        final BitTorrentClientConfig config2 = new BitTorrentClientConfig(PeerIdGeneratorTest.createDefault(), query, KeyGeneratorTest.createDefault(), defaultUrlEncoder, requestHeaders, 200, 0);

        assertThat(config).isEqualTo(config2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(PeerIdGeneratorTest.createDefault(), query, KeyGeneratorTest.createDefault(), defaultUrlEncoder, requestHeaders, 200, 0);
        final BitTorrentClientConfig config2 = new BitTorrentClientConfig(PeerIdGeneratorTest.createDefault(), query, KeyGeneratorTest.createDefault(), defaultUrlEncoder, requestHeaders, 200, 0);

        assertThat(config.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    public void shouldBuildClient() {
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final PeerIdGenerator peerIdGenerator = PeerIdGeneratorTest.createDefault();
        final KeyGenerator keyGenerator = KeyGeneratorTest.createDefault();
        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdGenerator, query, keyGenerator, defaultUrlEncoder, requestHeaders, 200, 0);

        final BitTorrentClient client = config.createClient();
        assertThat(client.getHeaders()).isEqualTo(requestHeaders.stream()
                .map(header -> new AbstractMap.SimpleEntry<>(header.getName(), header.getValue()))
                .collect(Collectors.toList())
        );
        assertThat(client.getQuery()).isEqualTo(query);
        //noinspection OptionalGetWithoutIsPresent
        assertThat(client.getKey(null, RequestEvent.STARTED).get()).isEqualTo(keyGenerator.getKey(null, RequestEvent.STARTED));
        assertThat(client.getPeerId(null, RequestEvent.STARTED)).isEqualTo(peerIdGenerator.getPeerId(null, RequestEvent.STARTED));
        assertThat(client.getNumwant(RequestEvent.STARTED)).isEqualTo(200);
    }

}
