package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class BitTorrentClientConfigSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldFailToDeserializeIfPeerIdInfoIsNotDefined() throws IOException {
        assertThatThrownBy(
                () -> mapper.readValue(
                        "{\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}],\"numwant\":100}",
                        BitTorrentClientConfig.class
                )
        )
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'peerIdInfo'");
        ;
    }

    @Test
    public void shouldFailToDeserializeIfQueryIsNotDefined() throws IOException {
        assertThatThrownBy(
                () -> mapper.readValue(
                        "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}],\"numwant\":100}",
                        BitTorrentClientConfig.class
                )
        )
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'query'");
        ;
    }

    @Test
    public void shouldNotFailToDeserializeIfKeyInfoIsNotDefined() throws IOException {
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(
                "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}],\"numwant\":100}",
                BitTorrentClientConfig.class
        );
        assertThat(bitTorrentClientConfig).isNotNull();
    }

    @Test
    public void shouldFailToDeserializeIfRequestHeadersIsNotDefined() throws IOException {
        assertThatThrownBy(
                () -> mapper.readValue(
                        "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"numwant\":100}",
                        BitTorrentClientConfig.class
                )
        )
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'requestHeaders'");
        ;
    }

    @Test
    public void shouldNotFailToDeserializeIfRequestHeadersIsEmpty() throws IOException {
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(
                "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[],\"numwant\":100}",
                BitTorrentClientConfig.class
        );
        assertThat(bitTorrentClientConfig).isNotNull();
    }

    @Test
    public void shouldFailToDeserializeIfNumwantIsNotDefined() throws IOException {
        assertThatThrownBy(
                () -> mapper.readValue(
                        "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}]}",
                        BitTorrentClientConfig.class
                )
        )
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'numwant'");
        ;
    }

    @Test
    public void shouldDeserialize() throws IOException {
        final BitTorrentClientConfig config = mapper.readValue(
                "{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}],\"numwant\":100}",
                BitTorrentClientConfig.class
        );
        assertThat(config).isNotNull();
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final KeyInfo keyInfo = new KeyInfo(8, ValueType.ALPHANUMERIC, false, false);
        final List<HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);

        assertThat(mapper.writeValueAsString(config))
                .isEqualTo("{\"peerIdInfo\":{\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyInfo\":{\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}],\"numwant\":100}");
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-AZ5750-", ValueType.ALPHANUMERIC, false, false);
        final String query = "info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3";
        final KeyInfo keyInfo = new KeyInfo(8, ValueType.ALPHANUMERIC, false, false);
        final List<HttpHeader> requestHeaders = Arrays.asList(
                new HttpHeader("User-Agent", "Azureus 5.7.5.0;{os};1.8.0_66"),
                new HttpHeader("Connection", "close"),
                new HttpHeader("Accept-Encoding", "gzip"),
                new HttpHeader("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2")
        );

        final BitTorrentClientConfig config = new BitTorrentClientConfig(peerIdInfo, query, keyInfo, requestHeaders, 100);

        assertThat(mapper.readValue(mapper.writeValueAsString(config), BitTorrentClientConfig.class)).isEqualToComparingFieldByField(config);
    }

}
