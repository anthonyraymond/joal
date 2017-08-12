package org.araymond.joal.core.client.emulated.generator.peerid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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
                        "    \"prefix\": \"-AA-\",\n" +
                        "    \"pattern\": \"[a-zA-Z0-9]\",\n" +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(NeverRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldSerializeNeverRefresh() throws IOException {
        final PeerIdGenerator generator = new NeverRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"NEVER\",\n" +
                                "  \"prefix\" : \"-AA-\",\n" +
                                "  \"pattern\" : \"[a-zA-Z0-9]\",\n"+
                                "  \"shouldUrlEncode\": false\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToAlwaysRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"ALWAYS\",\n" +
                        "    \"prefix\": \"-AA-\",\n" +
                        "    \"pattern\": \"[a-zA-Z0-9]\",\n" +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(AlwaysRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldSerializeAlwaysRefresh() throws IOException {
        final PeerIdGenerator generator = new AlwaysRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\" : \"ALWAYS\",\n" +
                                "  \"prefix\" : \"-AA-\",\n" +
                                "  \"pattern\" : \"[a-zA-Z0-9]\",\n"+
                                "  \"shouldUrlEncode\": false\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTimedRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TIMED\",\n" +
                        "    \"refreshEvery\": 60,\n" +
                        "    \"prefix\": \"-AA-\",\n" +
                        "    \"pattern\": \"[a-zA-Z0-9]\",\n" +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TimedRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldSerializeTimedRefresh() throws IOException {
        final PeerIdGenerator generator = new TimedRefreshPeerIdGenerator(60, "-AA-", "[a-zA-Z0-9]", false);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TIMED\",\n" +
                                "  \"refreshEvery\": 60,\n" +
                                "  \"prefix\" : \"-AA-\",\n" +
                                "  \"pattern\" : \"[a-zA-Z0-9]\",\n"+
                                "  \"shouldUrlEncode\": false\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentVolatileRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                        "    \"prefix\": \"-AA-\",\n" +
                        "    \"pattern\": \"[a-zA-Z0-9]\",\n" +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TorrentVolatileRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentVolatileRefresh() throws IOException {
        final PeerIdGenerator generator = new TorrentVolatileRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_VOLATILE\",\n" +
                                "  \"prefix\" : \"-AA-\",\n" +
                                "  \"pattern\" : \"[a-zA-Z0-9]\",\n"+
                                "  \"shouldUrlEncode\": false\n" +
                                "}"
                ));
    }

    @Test
    public void shouldDeserializeToTorrentPersistentRefresh() throws IOException {
        final String validJSON =
                "{\n" +
                        "    \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                        "    \"prefix\": \"-AA-\",\n" +
                        "    \"pattern\": \"[a-zA-Z0-9]\",\n" +
                        "    \"shouldUrlEncode\": false\n" +
                        "}";

        assertThat(mapper.readValue(validJSON, PeerIdGenerator.class))
                .isInstanceOf(TorrentPersistentRefreshPeerIdGenerator.class);
    }

    @Test
    public void shouldSerializeTorrentPersistentRefresh() throws IOException {
        final PeerIdGenerator generator = new TorrentPersistentRefreshPeerIdGenerator("-AA-", "[a-zA-Z0-9]", false);

        assertThat(mapper.readTree(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator)))
                .isEqualTo(mapper.readTree(
                        "{\n" +
                                "  \"refreshOn\": \"TORRENT_PERSISTENT\",\n" +
                                "  \"prefix\" : \"-AA-\",\n" +
                                "  \"pattern\" : \"[a-zA-Z0-9]\",\n"+
                                "  \"shouldUrlEncode\": false\n" +
                                "}"
                ));
    }

}
