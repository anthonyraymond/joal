package org.araymond.joal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.araymond.joal.core.bandwith.TorrentSeedStats;
import org.araymond.joal.core.bandwith.TorrentSeedStatsTest;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;
import org.araymond.joal.core.ttorrent.client.ConnectionHandlerTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by raymo on 20/07/2017.
 */
public class StaticClientFilesTester {

    private static final Path clientsPath = Paths.get("resources/clients");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldGenerateAnnounceURLAndHeaders() {
        FileUtils.listFiles(clientsPath.toFile(), new AcceptAllFileFilter(), null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = clientConfig.createClient();

                        final ConnectionHandler connHandler = ConnectionHandlerTest.createMockedIpv4(1111);
                        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

                        client.createRequestQuery(RequestEvent.STARTED, new InfoHash("adb".getBytes()), stats, connHandler);
                        client.createRequestHeaders();
                    } catch (final Exception e) {
                        fail("Exception for client file " + file.getName(), e);
                    }
                });
    }

    private static class AcceptAllFileFilter extends AbstractFileFilter {
        @Override
        public boolean accept(final File file) {
            return true;
        }
    }

    @Test
    public void shouldBeDeserializable() {
        FileUtils.listFiles(clientsPath.toFile(), new AcceptAllFileFilter(), null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = clientConfig.createClient();


                        final String query = client.getQuery();
                        assertThat(query)
                                .contains("info_hash={infohash}")
                                .contains("peer_id={peerid}")
                                .contains("uploaded={uploaded}")
                                .contains("downloaded={downloaded}")
                                .contains("left={left}")
                                .contains("key={key}")
                                .contains("event={event}");
                        if (!file.getName().contains("rtorrent")) {
                                assertThat(query).contains("numwant={numwant}");
                        }
                        if (query.contains("ipv6=")) assertThat(query).contains("ipv6={ipv6}");
                        if (query.contains("ip=")) assertThat(query).contains("ip={ip}");
                        if (query.contains("{ipv6}")) assertThat(query).contains("ipv6={ipv6}");
                        if (query.contains("{ip}")) assertThat(query).contains("ip={ip}");
                        assertThat(query).doesNotContain("&&");
                        assertThat(query).doesNotStartWith("&");
                        assertThat(query).doesNotEndWith("&");
                    } catch (final Exception e) {
                        fail("Exception for client file " + file.getName(), e);
                    }
                });
    }
}
