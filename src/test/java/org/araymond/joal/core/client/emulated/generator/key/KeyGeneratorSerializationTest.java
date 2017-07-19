package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.client.emulated.generator.key.type.KeyTypes;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class KeyGeneratorSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldDeserializeToNeverRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"NEVER\",\n" +
                        "    \"length\": 8,\n" +
                        "    \"type\": \"hash\",\n" +
                        "    \"upperCase\": false,\n" +
                        "    \"lowerCase\": true\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(NeverRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeNeverRefresh() throws IOException {
        final KeyGenerator generator = new NeverRefreshKeyGenerator(8, KeyTypes.HASH, false, true);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"NEVER\",\n" +
                                "  \"length\" : 8,\n" +
                                "  \"type\" : \"hash\",\n" +
                                "  \"upperCase\" : false,\n" +
                                "  \"lowerCase\" : true\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToAlwaysRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"ALWAYS\",\n" +
                        "    \"length\": 8,\n" +
                        "    \"type\": \"hash\",\n" +
                        "    \"upperCase\": false,\n" +
                        "    \"lowerCase\": true\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(AlwaysRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeAlwaysRefresh() throws IOException {
        final KeyGenerator generator = new AlwaysRefreshKeyGenerator(8, KeyTypes.HASH, false, true);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"ALWAYS\",\n" +
                                "  \"length\" : 8,\n" +
                                "  \"type\" : \"hash\",\n" +
                                "  \"upperCase\" : false,\n" +
                                "  \"lowerCase\" : true\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTimedRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TIMED\",\n" +
                        "    \"refreshEvery\": 60,\n" +
                        "    \"length\": 8,\n" +
                        "    \"type\": \"hash\",\n" +
                        "    \"upperCase\": false,\n" +
                        "    \"lowerCase\": true\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TimedRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTimedRefresh() throws IOException {
        final KeyGenerator generator = new TimedRefreshKeyGenerator(60, 8, KeyTypes.HASH, false, true);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TIMED\",\n" +
                                "  \"refreshEvery\": 60,\n" +
                                "  \"length\" : 8,\n" +
                                "  \"type\" : \"hash\",\n" +
                                "  \"upperCase\" : false,\n" +
                                "  \"lowerCase\" : true\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentVolatileRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                        "    \"length\": 8,\n" +
                        "    \"type\": \"hash\",\n" +
                        "    \"upperCase\": false,\n" +
                        "    \"lowerCase\": true\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TorrentVolatileRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentVolatileRefresh() throws IOException {
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(8, KeyTypes.HASH, false, true);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                                "  \"length\" : 8,\n" +
                                "  \"type\" : \"hash\",\n" +
                                "  \"upperCase\" : false,\n" +
                                "  \"lowerCase\" : true\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentPersistentRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                        "    \"length\": 8,\n" +
                        "    \"type\": \"hash\",\n" +
                        "    \"upperCase\": false,\n" +
                        "    \"lowerCase\": true\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TorrentPersistentRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentPersistentRefresh() throws IOException {
        final KeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(8, KeyTypes.HASH, false, true);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                                "  \"length\" : 8,\n" +
                                "  \"type\" : \"hash\",\n" +
                                "  \"upperCase\" : false,\n" +
                                "  \"lowerCase\" : true\n" +
                                "}"
                ));
    }

}
