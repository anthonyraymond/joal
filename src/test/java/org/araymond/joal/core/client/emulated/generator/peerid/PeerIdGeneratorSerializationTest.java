package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by raymo on 16/07/2017.
 */
public class PeerIdGeneratorSerializationTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldDeserializeToNeverRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"NEVER\",\n" +
                        "    \"algorithm\": {\n" +
                        "        \"type\": \"REGEX\",\n" +
                        "        \"pattern\": \"-lt0D60-[\\u0001-\\u00ff]{12}\"\n" +
                        "    }," +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(NeverRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldDeserializeToAlwaysRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"ALWAYS\",\n" +
                        "    \"algorithm\": {\n" +
                        "        \"type\": \"REGEX\",\n" +
                        "        \"pattern\": \"-lt0D60-[\\u0001-\\u00ff]{12}\"\n" +
                        "    }," +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(AlwaysRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldDeserializeToTimedRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TIMED\",\n" +
                        "    \"refreshEvery\": 60,\n" +
                        "    \"algorithm\": {\n" +
                        "        \"type\": \"REGEX\",\n" +
                        "        \"pattern\": \"-lt0D60-[\\u0001-\\u00ff]{12}\"\n" +
                        "    }," +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TimedRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldDeserializeToTorrentVolatileRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                        "    \"algorithm\": {\n" +
                        "        \"type\": \"REGEX\",\n" +
                        "        \"pattern\": \"-lt0D60-[\\u0001-\\u00ff]{12}\"\n" +
                        "    }," +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TorrentVolatileRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldDeserializeToTorrentPersistentRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                        "    \"algorithm\": {\n" +
                        "        \"type\": \"REGEX\",\n" +
                        "        \"pattern\": \"-lt0D60-[\\u0001-\\u00ff]{12}\"\n" +
                        "    }," +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TorrentPersistentRefreshPeerIdGenerator.class);
    }

}
