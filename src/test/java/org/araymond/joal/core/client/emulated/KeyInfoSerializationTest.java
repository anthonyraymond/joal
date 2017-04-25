package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.*;
import static org.araymond.joal.core.client.emulated.BitTorrentClientConfig.ValueType.ALPHABETIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class KeyInfoSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldFailDeserializeIfLengthIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}", KeyInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'length'");
    }

    @Test
    public void shouldFailDeserializeIfTypeIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"length\":8,\"upperCase\":false,\"lowerCase\":true}", KeyInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'type'");
    }

    @Test
    public void shouldFailDeserializeIfUpperCaseIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"length\":8,\"type\":\"alphabetic\",\"lowerCase\":true}", KeyInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'upperCase'");
    }

    @Test
    public void shouldFailDeserializeIfLowerCaseIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"length\":8,\"type\":\"alphabetic\",\"upperCase\":false}", KeyInfo.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'lowerCase'");
    }


    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final KeyInfo keyInfo = new KeyInfo(8, ALPHABETIC, false, true);
        assertThat(mapper.writeValueAsString(keyInfo)).isEqualTo("{\"length\":8,\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}");
    }


    @Test
    public void shouldDeserialize() throws IOException {
        final KeyInfo keyInfo = mapper.readValue("{\"length\":8,\"type\":\"alphabetic\",\"upperCase\":false,\"lowerCase\":true}", KeyInfo.class);
        assertThat(keyInfo.getLength()).isEqualTo(8);
        assertThat(keyInfo.getType()).isEqualTo(ALPHABETIC);
        assertThat(keyInfo.isUpperCase()).isFalse();
        assertThat(keyInfo.isLowerCase()).isTrue();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final KeyInfo keyInfo = new KeyInfo(8, ALPHABETIC, false, true);
        assertThat(mapper.readValue(mapper.writeValueAsString(keyInfo), KeyInfo.class)).isEqualToComparingFieldByField(keyInfo);
    }

}
