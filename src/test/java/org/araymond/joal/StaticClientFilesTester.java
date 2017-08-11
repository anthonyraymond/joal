package org.araymond.joal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStatsTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator.PEER_ID_LENGTH;
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

    @Test
    public void shouldBeDeserializable() {
        FileUtils.listFiles(clientsPath.toFile(), new AcceptAllFileFilter(), null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = clientConfig.createClient();

                        if (file.getName().contains("deluge")) {
                            System.out.println("o");
                        }
                        final String query = client.getQuery();
                        assertThat(query)
                                .contains("info_hash={infohash}")
                                .contains("peer_id={peerid}")
                                .contains("uploaded={uploaded}")
                                .contains("downloaded={downloaded}")
                                .contains("left={left}")
                                .contains("key={key}")
                                .contains("event={event}")
                                .contains("numwant={numwant}");
                        if (query.contains("ipv6=")) assertThat(query).contains("ipv6={ipv6}");
                        if (query.contains("ip=")) assertThat(query).contains("ip={ip}");
                        if (query.contains("{ipv6}")) assertThat(query).contains("ipv6={ipv6}");
                        if (query.contains("{ip}")) assertThat(query).contains("ip={ip}");
                    } catch (final Exception e) {
                        fail("Exception for client file " + file.getName(), e);
                    }
                });
    }

    @Test
    public void shouldMatchPeerIdPattern() {
        FileUtils.listFiles(clientsPath.toFile(), new AcceptAllFileFilter(), null)
                .forEach(file -> {
                    IntStream.range(1, 30).forEach(i -> {
                        try {
                            final String json = new String(Files.readAllBytes(file.toPath()));
                            final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                            final BitTorrentClient client = clientConfig.createClient();

                            final String peerIdPattern = extractStringPropertyFromJson("pattern", json);
                            final String peerIdPrefix = extractStringPropertyFromJson("prefix", json);
                            final boolean isUrlEncode = extractBoolPropertyFromJson("isUrlEncoded", json);

                            String peerId = client.getPeerId(null, RequestEvent.STARTED);
                            if (isUrlEncode) {
                                peerId = URLDecoder.decode(peerId, Torrent.BYTE_ENCODING);
                            }
                            assertThat(peerId).hasSize(20);
                            assertThat(peerId)
                                    .as(file.getName() + " => " + peerId)
                                    .matches(peerIdPrefix + peerIdPattern);
                        } catch (final Exception e) {
                            fail("Exception for client file " + file.getName(), e);
                        }
                    });
                });
    }

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
    }

    private static class AcceptAllFileFilter extends AbstractFileFilter {
        @Override
        public boolean accept(final File file) {
            return true;
        }
    }

    private String extractStringPropertyFromJson(final String propertyName, final String json) throws IllegalStateException {
        final Matcher matcher = Pattern.compile(propertyName + "\": \"(.*?)\"([\r\n,])").matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Failed to extract property '" + propertyName + "'");
        }
    }
    private boolean extractBoolPropertyFromJson(final String propertyName, final String json) throws IllegalStateException {
        final Matcher matcher = Pattern.compile(propertyName + "\": (true|false)([\r\n,])").matcher(json);
        if (matcher.find()) {
            final String match = matcher.group(1);
            if ("true".equals(match)) {
                return true;
            } else if ("false".equals(match)) {
                return false;
            } else {
                throw new IllegalStateException("Failed to extract property '" + propertyName + "', it was nor true nor false.");
            }
        } else {
            throw new IllegalStateException("Failed to extract property '" + propertyName + "'");
        }
    }
}
