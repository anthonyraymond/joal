package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class BitTorrentClientConfigSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String validJSON = "{\"peerIdGenerator\":{\"refreshOn\":\"NEVER\",\"prefix\":\"-AZ5750-\",\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyGenerator\":{\"refreshOn\":\"NEVER\",\"length\":8,\"type\":\"alphanumeric\",\"upperCase\":false,\"lowerCase\":false},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}]}";

    @Test
    public void shouldFailToDeserializeIfPeerIdInfoIsNotDefined() throws IOException {
        final String jsonWithoutPeerId = validJSON.replaceAll("\"peerIdGenerator\":[ ]*\\{.*?},", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutPeerId, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'peerIdGenerator'");
    }

    @Test
    public void shouldFailToDeserializeIfQueryIsNotDefined() throws IOException {
        final String jsonWithoutQuery = validJSON.replaceAll("\"query\":[ ]*\".*?\",", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutQuery, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'query'");
    }

    @Test
    public void shouldNotFailToDeserializeIfKeyGeneratorIsNotDefined() throws IOException {
        final String jsonWithoutKeyGenerator = validJSON
                .replaceAll("\"keyGenerator\":[ ]*\\{.*?},", "")
                .replaceAll("&key=\\{key}", "");
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(jsonWithoutKeyGenerator, BitTorrentClientConfig.class);

        assertThat(bitTorrentClientConfig).isNotNull();
        assertThat(bitTorrentClientConfig.createClient().getKey(null, RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldFailToDeserializeIfRequestHeadersIsNotDefined() throws IOException {
        final String jsonWithoutHeaders = validJSON.replaceAll(",\"requestHeaders\":[ ]*\\[.*?\\]", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutHeaders, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'requestHeaders'");
    }

    @Test
    public void shouldNotFailToDeserializeIfRequestHeadersIsEmpty() throws IOException {
        final String jsonWithEmptyHeaders = validJSON.replaceAll("\"requestHeaders\":[ ]*\\[.*?\\]", "\"requestHeaders\":[]");
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(jsonWithEmptyHeaders, BitTorrentClientConfig.class);

        assertThat(bitTorrentClientConfig).isNotNull();
        assertThat(bitTorrentClientConfig.createClient().getHeaders()).hasSize(0);
    }

    @Test
    public void shouldDeserialize() throws IOException {
        final BitTorrentClientConfig config = mapper.readValue(validJSON, BitTorrentClientConfig.class);

        assertThat(config).isNotNull();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        assertThat(mapper.readTree(mapper.writeValueAsString(mapper.readValue(validJSON, BitTorrentClientConfig.class))))
                .isEqualTo(mapper.readTree(validJSON));
    }

}
