package org.araymond.joal.core.client.emulated.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlEncoderSerializationTest {

    private final String json = "{\"encodingExclusionPattern\":\"[a-z]\",\"encodedHexCase\":\"lower\"}";

    @Test
    public void shouldDeserialize() throws IOException {
        final UrlEncoder urlEncoder = new ObjectMapper().readValue(json, UrlEncoder.class);

        assertThat(urlEncoder.getEncodingExclusionPattern()).isEqualTo("[a-z]");
        assertThat(urlEncoder.getEncodedHexCase()).isEqualTo(UrlEncoder.Casing.LOWER);
    }

    @Test
    public void shouldSerialize() throws IOException {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-z]", UrlEncoder.Casing.LOWER);

        assertThat(new ObjectMapper().writeValueAsString(urlEncoder)).isEqualTo(json);
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final UrlEncoder urlEncoder = new UrlEncoder("[a-z]", UrlEncoder.Casing.LOWER);

        assertThat(new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(urlEncoder), UrlEncoder.class))
                .isEqualTo(urlEncoder);
    }

}
