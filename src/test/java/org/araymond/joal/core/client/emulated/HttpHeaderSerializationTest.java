package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class HttpHeaderSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldFailToDeserializeIfNameIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"value\":\"close\"}", HttpHeader.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'name'");
    }

    @Test
    public void shouldFailToDeserializeIfValueIsNotDefined() {
        assertThatThrownBy(() -> mapper.readValue("{\"name\":\"Connection\"}", HttpHeader.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'value'");
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final HttpHeader header = new HttpHeader("Connection", "close");
        assertThat(mapper.writeValueAsString(header)).isEqualTo(
                "{\"name\":\"Connection\",\"value\":\"close\"}"
        );
    }

    @Test
    public void shouldDeserialize() throws IOException {
        final HttpHeader header = mapper.readValue("{\"name\":\"Connection\",\"value\":\"close\"}", HttpHeader.class);
        assertThat(header.getName()).isEqualTo("Connection");
        assertThat(header.getValue()).isEqualTo("close");
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final HttpHeader header = new HttpHeader("Connection", "close");
        final HttpHeader deserHdr = mapper.readValue(mapper.writeValueAsString(header), HttpHeader.class);
        assertThat(deserHdr).isEqualTo(header);
    }

}
