package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Created by raymo on 24/04/2017.
 */
public class BitTorrentClientConfigSerializationTest {

    private BitTorrentClientProvider provider = new BitTorrentClientProvider(mock(JoalConfigProvider.class),
            mock(ObjectMapper.class), mock(SeedManager.JoalFoldersPath.class));
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String validJSON = "{\"peerIdGenerator\":{\"refreshOn\":\"NEVER\",\"algorithm\": { \"type\": \"REGEX\", \"pattern\": \"-AZ5750-[a-zA-Z0-9]{12}\" }, \"shouldUrlEncode\": false},\"urlEncoder\":{\"encodingExclusionPattern\":\"[a-z]\",\"encodedHexCase\":\"lower\"},\"numwant\":200,\"numwantOnStop\":0,\"query\":\"info_hash={infohash}&peer_id={peerid}&supportcrypto=1&port={port}&azudp={port}&uploaded={uploaded}&downloaded={downloaded}&left={left}&corrupt=0&event={event}&numwant={numwant}&no_peer_id=1&compact=1&key={key}&azver=3\",\"keyGenerator\":{\"refreshOn\":\"NEVER\",\"algorithm\": { \"type\": \"HASH\", \"length\": 8 },\"keyCase\":\"lower\"},\"requestHeaders\":[{\"name\":\"User-Agent\",\"value\":\"Azureus 5.7.5.0;{os};1.8.0_66\"},{\"name\":\"Connection\",\"value\":\"close\"},{\"name\":\"Accept-Encoding\",\"value\":\"gzip\"},{\"name\":\"Accept\",\"value\":\"text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\"}]}";

    @Test
    public void shouldFailToDeserializeIfPeerIdInfoIsNotDefined() throws IOException {
        final String jsonWithoutPeerId = validJSON.replaceAll("\"peerIdGenerator\": *\\{.*?},", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutPeerId, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'peerIdGenerator'");
    }

    @Test
    public void shouldFailToDeserializeIfQueryIsNotDefined() throws IOException {
        final String jsonWithoutQuery = validJSON.replaceAll("\"query\": *\".*?\",", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutQuery, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'query'");
    }

    @Test
    public void shouldNotFailToDeserializeIfKeyGeneratorIsNotDefined() throws IOException {
        final String jsonWithoutKeyGenerator = validJSON
                .replaceAll("\"keyGenerator\": *\\{.*?algorithm\": *\\{.*?}.*?},", "")
                .replaceAll("&key=\\{key}", "");
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(jsonWithoutKeyGenerator, BitTorrentClientConfig.class);

        assertThat(bitTorrentClientConfig).isNotNull();
        assertThat(provider.createClient(bitTorrentClientConfig).getKey(null, RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldFailToDeserializeIfUrlEncoderIsNotDefined() throws IOException {
        final String jsonWithoutUrlEncoder = validJSON.replaceAll("\"urlEncoder\": *\\{.*?},", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutUrlEncoder, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'urlEncoder'");
    }

    @Test
    public void shouldFailToDeserializeIfNumwantIsNotDefined() throws IOException {
        final String jsonWithoutNumwant = validJSON.replaceAll("\"numwant\": *\\d+,", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutNumwant, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'numwant'");
    }

    @Test
    public void shouldFailToDeserializeIfNumwantOnStopIsNotDefined() throws IOException {
        final String jsonWithoutNumwantOnStop = validJSON.replaceAll("\"numwantOnStop\": *\\d+,", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutNumwantOnStop, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'numwantOnStop'");
    }

    @Test
    public void shouldFailToDeserializeIfRequestHeadersIsNotDefined() throws IOException {
        final String jsonWithoutHeaders = validJSON.replaceAll(",\"requestHeaders\": *\\[.*?]", "");
        assertThatThrownBy(() -> mapper.readValue(jsonWithoutHeaders, BitTorrentClientConfig.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'requestHeaders'");
    }

    @Test
    public void shouldNotFailToDeserializeIfRequestHeadersIsEmpty() throws IOException {
        final String jsonWithEmptyHeaders = validJSON.replaceAll("\"requestHeaders\": *\\[.*?]", "\"requestHeaders\":[]");
        final BitTorrentClientConfig bitTorrentClientConfig = mapper.readValue(jsonWithEmptyHeaders, BitTorrentClientConfig.class);

        assertThat(bitTorrentClientConfig).isNotNull();
        assertThat(provider.createClient(bitTorrentClientConfig).getHeaders()).hasSize(0);
    }

    @Test
    public void shouldDeserialize() throws IOException {
        final BitTorrentClientConfig config = mapper.readValue(validJSON, BitTorrentClientConfig.class);

        assertThat(config).isNotNull();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        BitTorrentClientConfig deserConf = mapper.readValue(validJSON, BitTorrentClientConfig.class);
        assertThat(mapper.readTree(mapper.writeValueAsString(deserConf)))
                .isEqualTo(mapper.readTree(validJSON));
    }

}
