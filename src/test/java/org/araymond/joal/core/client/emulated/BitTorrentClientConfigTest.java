package org.araymond.joal.core.client.emulated;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class BitTorrentClientConfigTest {

    static final BitTorrentClientConfig defaultClientConfig = new BitTorrentClientConfig(
            new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false),
            "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3",
            new KeyInfo(8, ValueType.ALPHANUMERIC, false, false),
            Arrays.asList(
                    new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                    new HttpHeader("Connection", "close"),
                    new HttpHeader("Accept-Encoding", "gzip"),
                    new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
            ),
            100
    );

    @Test
    public void shouldCreateClientWithDifferentKeyAndPeerIdEachTime() {
        for (int i = 0; i < 20; ++i) {
            final BitTorrentClient client1 = defaultClientConfig.createClient();
            final BitTorrentClient client2 = defaultClientConfig.createClient();

            assertThat(client1.getKey().get()).isNotEqualTo(client2.getKey().get());
            assertThat(client1.getPeerId()).isNotEqualTo(client2.getPeerId());
        }
    }

    @Test
    public void ShouldBuildEvenIfKeyInfoIsNotDefined() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdInfo, query, null, requestHeaders, 100);

        assertThat(config.createClient().getKey()).isEmpty();
    }

    @Test
    public void ShouldNotBuildIfKeyInfoIsNotDefinedButKeyIsInQueryUrl() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        assertThatThrownBy(() -> new BitTorrentClientConfig(peerIdInfo, query, null, requestHeaders, 100))
                .isInstanceOf(TorrentClientConfigIntegrityException.class)
                .hasMessageContaining("Query string contains {key}, but no keyInfo was found in .client file.");
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final KeyInfo keyInfo = new KeyInfo(8, ValueType.ALPHANUMERIC, false, false);
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);
        final BitTorrentClientConfig config2 = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);

        assertThat(config).isEqualTo(config2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final KeyInfo keyInfo = new KeyInfo(8, ValueType.ALPHANUMERIC, false, false);
        final List<BitTorrentClientConfig.HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);
        final BitTorrentClientConfig config2 = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);

        assertThat(config.hashCode()).isEqualTo(config2.hashCode());
    }

}
