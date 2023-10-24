package org.araymond.joal.core.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by raymo on 24/04/2017.
 */
public class AppConfigurationSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldFailToDeserializeIfMinUploadRateIsNotDefined() throws IOException {
        assertThatThrownBy(() -> mapper.readValue("{\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false}", AppConfiguration.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'minUploadRate'");
    }

    @Test
    public void shouldFailToDeserializeIfMaxUploadRateIsNotDefined() throws IOException {
        assertThatThrownBy(() -> mapper.readValue("{\"minUploadRate\":180,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false}", AppConfiguration.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'maxUploadRate'");
    }

    @Test
    public void shouldFailToDeserializeIfSimultaneousSeedIsNotDefined() throws IOException {
        assertThatThrownBy(() -> mapper.readValue("{\"minUploadRate\":180,\"maxUploadRate\":190,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false}", AppConfiguration.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'simultaneousSeed'");
    }

    @Test
    public void shouldFailToDeserializeIfClientIsNotDefined() throws IOException {
        assertThatThrownBy(() -> mapper.readValue("{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"keepTorrentWithZeroLeechers\":false}", AppConfiguration.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'client'");
    }

    @Test
    public void shouldFailToDeserializeIfKeepTorrentWithZeroLeechersIsNotDefined() throws IOException {
        assertThatThrownBy(() -> mapper.readValue("{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\"}", AppConfiguration.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("Missing required creator property 'keepTorrentWithZeroLeechers'");
    }

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final AppConfiguration config = new AppConfiguration(180L, 190L, 2, "azureus.client", false, 1f);
        assertThat(mapper.writeValueAsString(config)).isEqualTo("{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false,\"uploadRatioTarget\":1.0}");
    }

    @Test
    public void shouldDeserialize() throws IOException {
        final AppConfiguration config = mapper.readValue(
                "{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false}",
                AppConfiguration.class
        );
        assertThat(config.getMinUploadRate()).isEqualTo(180);
        assertThat(config.getMaxUploadRate()).isEqualTo(190);
        assertThat(config.getSimultaneousSeed()).isEqualTo(2);
        assertThat(config.getClient()).isEqualTo("azureus.client");
        assertThat(config.isKeepTorrentWithZeroLeechers()).isEqualTo(false);
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        final AppConfiguration config = mapper.readValue(
                "{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false}",
                AppConfiguration.class
        );

        assertThat(mapper.readValue(mapper.writeValueAsString(config), AppConfiguration.class)).usingRecursiveComparison().isEqualTo(config);
    }

    @Test
    public void shouldIgnoreUnknownProperties() throws IOException {
        // If the config is a merge of multiple updates and some properties have been removed it will result in useless properties
        // Ensure unknown properties won't make it crash
        final AppConfiguration config = mapper.readValue(
                "{\"minUploadRate\":180,\"maxUploadRate\":190,\"simultaneousSeed\":2,\"client\":\"azureus.client\",\"keepTorrentWithZeroLeechers\":false,\"qddlqjdqlskdjlqk\":\"qdqdqsdd\"}",
                AppConfiguration.class
        );
        assertThat(config.getMinUploadRate()).isEqualTo(180);
        assertThat(config.getMaxUploadRate()).isEqualTo(190);
        assertThat(config.getSimultaneousSeed()).isEqualTo(2);
        assertThat(config.getClient()).isEqualTo("azureus.client");
    }

}
