package org.araymond.joal.core.client.emulated.generator.key;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.HashKeyAlgorithm;
import org.araymond.joal.core.client.emulated.generator.key.algorithm.KeyAlgorithm;
import org.araymond.joal.core.client.emulated.utils.Casing;
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
                        "  \"refreshOn\": \"NEVER\",\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(NeverRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeNeverRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new NeverRefreshKeyGenerator(algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"NEVER\",\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToAlwaysRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "  \"refreshOn\": \"ALWAYS\",\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(AlwaysRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeAlwaysRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new AlwaysRefreshKeyGenerator(algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"ALWAYS\",\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTimedRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "  \"refreshOn\": \"TIMED\",\n" +
                        "  \"refreshEvery\": 60,\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TimedRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTimedRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new TimedRefreshKeyGenerator(60, algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TIMED\",\n" +
                                "  \"refreshEvery\": 60,\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTimedOrAfterStartedRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "  \"refreshOn\": \"TIMED_OR_AFTER_STARTED_ANNOUNCE\",\n" +
                        "  \"refreshEvery\": 60,\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TimedOrAfterStartedAnnounceRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTimedOrAfterStartedRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new TimedOrAfterStartedAnnounceRefreshKeyGenerator(60, algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TIMED_OR_AFTER_STARTED_ANNOUNCE\",\n" +
                                "  \"refreshEvery\": 60,\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentVolatileRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "  \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TorrentVolatileRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentVolatileRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new TorrentVolatileRefreshKeyGenerator(algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentPersistentRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "  \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                        "  \"algorithm\": {\n" +
                        "    \"type\": \"HASH\",\n" +
                        "    \"length\": 8\n" +
                        "  },\n" +
                        "  \"keyCase\" : \"lower\"\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, KeyGenerator.class))
                .isInstanceOf(TorrentPersistentRefreshKeyGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentPersistentRefresh() throws IOException {
        final KeyAlgorithm algo = new HashKeyAlgorithm(8);
        final KeyGenerator generator = new TorrentPersistentRefreshKeyGenerator(algo, Casing.LOWER);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                                "  \"algorithm\": {\n" +
                                "    \"type\": \"HASH\",\n" +
                                "    \"length\": 8\n" +
                                "  },\n" +
                                "  \"keyCase\" : \"lower\"\n" +
                                "}"
                ));
    }

}
