package org.araymond.joal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStatsTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
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

    private static InetAddress createMockedINet6Address() {
        try {
            return InetAddress.getByName("fd2d:7212:4cd5:2f14:ffff:ffff:ffff:ffff");
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ConnectionHandler createMockedConnectionHandler(final InetAddress inetAddress) {
        final ConnectionHandler connectionHandler = Mockito.mock(ConnectionHandler.class);
        Mockito.when(connectionHandler.getPort()).thenReturn(46582);
        Mockito.when(connectionHandler.getIpAddress()).thenReturn(inetAddress);
        return connectionHandler;
    }

    //TODO: fix it with new call
    /*
    @Test
    public void shouldGenerateAnnounceURL() {
        FileUtils.listFiles(clientsPath.toFile(), new AcceptAllFileFilter(), null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = clientConfig.createClient();

                        final ConnectionHandler connHandler = createMockedConnectionHandler(createMockedINet6Address());
                        final TorrentWithStats torrent = TorrentWithStatsTest.createMocked();

                        client.buildAnnounceRequest(new URL("http://my.tracker.com/announce"), RequestEvent.STARTED, torrent, connHandler);
                    } catch (final Exception e) {
                        fail("Exception for client file " + file.getName(), e);
                    }
                });
    }*/

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
                    } catch (final Exception e) {
                        fail("Exception for client file " + file.getName(), e);
                    }
                });
    }

    private String extractStringPropertyFromJson(final String propertyName, final String json) throws IllegalStateException {
        final Matcher matcher = Pattern.compile(propertyName + "\": \"(.*?)\"([\r\n,])").matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Failed to extract property '" + propertyName + "'");
        }
    }
}
