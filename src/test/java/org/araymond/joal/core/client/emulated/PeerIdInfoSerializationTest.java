package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig.PeerIdInfo;
import org.junit.Test;

import java.io.IOException;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.ValueType.ALPHABETIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class PeerIdInfoSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldFailDeserializeIfPrefixIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}", PeerIdInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'prefix'");
    }

    @Test
    public void shouldFailDeserializeIfTypeIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"prefix\":\"-my.pre-\",\"upperCase\":false,\"lowerCase\":true}", PeerIdInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'type'");
    }

    @Test
    public void shouldFailDeserializeIfUpperCaseIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"prefix\":\"-my.pre-\",\"type\":\"alphabetic\",\"lowerCase\":true}", PeerIdInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'upperCase'");
    }

    @Test
    public void shouldFailDeserializeIfLowerCaseIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"prefix\":\"-my.pre-\",\"type\":\"alphabetic\",\"upperCase\":false}", PeerIdInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'lowerCase'");
    }


    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        assertThat(mapper.writeValueAsString(peerIdInfo)).isEqualTo("{\"prefix\":\"-my.pre-\",\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}");
    }


    @Test
    public void shouldDeserialize() throws IOException {
        final PeerIdInfo peerIdInfo = mapper.readValue("{\"prefix\":\"-my.pre-\",\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}", PeerIdInfo.class);
        assertThat(peerIdInfo.getPrefix()).isEqualTo("-my.pre-");
        assertThat(peerIdInfo.getType()).isEqualTo(ALPHABETIC);
        assertThat(peerIdInfo.isUpperCase()).isFalse();
        assertThat(peerIdInfo.isLowerCase()).isTrue();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final PeerIdInfo peerIdInfo = new PeerIdInfo("-my.pre-", ALPHABETIC, false, true);
        assertThat(mapper.readValue(mapper.writeValueAsString(peerIdInfo), PeerIdInfo.class)).isEqualToComparingFieldByField(peerIdInfo);
    }

}
