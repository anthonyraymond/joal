package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.bandwith.TorrentSeedStats;
import org.araymond.joal.core.bandwith.TorrentSeedStatsTest;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig.HttpHeader;
import org.araymond.joal.core.client.emulated.generator.UrlEncoder;
import org.araymond.joal.core.client.emulated.generator.UrlEncoderTest;
import org.araymond.joal.core.client.emulated.generator.key.KeyGenerator;
import org.araymond.joal.core.client.emulated.generator.key.KeyGeneratorTest;
import org.araymond.joal.core.client.emulated.generator.numwant.NumwantProvider;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGenerator;
import org.araymond.joal.core.client.emulated.generator.peerid.PeerIdGeneratorTest;
import org.araymond.joal.core.client.emulated.utils.Casing;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.exception.UnrecognizedClientPlaceholder;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;
import org.araymond.joal.core.ttorrent.client.ConnectionHandlerTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by raymo on 26/04/2017.
 */
public class BitTorrentClientTest {

    private final KeyGenerator defaultKeyGenerator = KeyGeneratorTest.createDefault();
    private final PeerIdGenerator defaultPeerIdGenerator = PeerIdGeneratorTest.createDefault();
    private final UrlEncoder defaultUrlEncoder = new UrlEncoder(".*", Casing.LOWER);
    private final NumwantProvider defaultNumwantProvider = new NumwantProvider(200, 0);

    private static final Path clientsPath = Paths.get("resources/clients");
    private static final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldCreateHeadersInSameOrderAndReplacePlaceHolders() {
        final List<HttpHeader> headers = new ArrayList<>();
        headers.add(new HttpHeader("java-version", "{java}"));
        headers.add(new HttpHeader("os", "{os}"));
        headers.add(new HttpHeader("Connection", "close"));
        headers.add(new HttpHeader("locale", "{locale}"));

        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", headers, defaultNumwantProvider);

        assertThat(client.createRequestHeaders()).containsExactly(
                new AbstractMap.SimpleEntry<>("java-version", getProperty("java.version")),
                new AbstractMap.SimpleEntry<>("os", getProperty("os.name")),
                new AbstractMap.SimpleEntry<>("Connection", "close"),
                new AbstractMap.SimpleEntry<>("locale", Locale.getDefault().toLanguageTag())
        );
    }

    @Test
    public void shouldReplacePlaceHolderThatAppearsMultipleTimes() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();


        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                null,
                defaultUrlEncoder,
                "key={key}&event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThatThrownBy(() -> client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Client request query contains 'key' but BitTorrentClient does not have a key.");
    }

    @Test
    public void shouldReplaceNumwantAndNumwantOnStop() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();
        final NumwantProvider numwantProvider = new NumwantProvider(10, 50);

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "numwant={numwant}",
                Collections.emptyList(),
                numwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("numwant=10");
        assertThat(client.createRequestQuery(RequestEvent.NONE, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("numwant=10");
        assertThat(client.createRequestQuery(RequestEvent.STOPPED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("numwant=50");
    }

    @Test
    public void shouldReplaceIpv4AndIpv6() {
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "ipv4={ip}&ipv6={ipv6}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        // If Ipv4, should fill param and remove {ipv6}
        final ConnectionHandler mockedIpv4 = ConnectionHandlerTest.createMockedIpv4(12);
        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, mockedIpv4))
                .isEqualToIgnoringCase("ipv4=" + mockedIpv4.getIpAddress().getHostAddress());
        // If Ipv6, should fill param and remove {ip}
        final ConnectionHandler mockedIpv6 = ConnectionHandlerTest.createMockedIpv6(12);
        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, mockedIpv6))
                .isEqualToIgnoringCase("ipv6=" + mockedIpv6.getIpAddress().getHostAddress());
    }

    @Test
    public void shouldReplacePort() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "port={port}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("port=12345");
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldReplaceTorrentStats() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = mock(TorrentSeedStats.class);
        doReturn(123456L).when(stats).getUploaded();
        doReturn(1478L).when(stats).getDownloaded();
        doReturn(369L).when(stats).getLeft();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "uploaded={uploaded}&downloaded={downloaded}&left={left}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("uploaded=123456&downloaded=1478&left=369");
    }

    @Test
    public void shouldFailIfPlaceHoldersRemainsInURL() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "nop={wtfIsThisPlaceHolder}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThatThrownBy(() -> client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isInstanceOf(UnrecognizedClientPlaceholder.class);
    }

    @Test
    public void shouldUrlEncodeInfoHash() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                new UrlEncoder("", Casing.UPPER),
                "infohash={infohash}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("infohash=%61");
    }

    @Test
    public void shouldUrlEncodePeerId() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                PeerIdGeneratorTest.createForPattern("-AA-[a]{16}", true),
                defaultKeyGenerator,
                new UrlEncoder("", Casing.UPPER),
                "peerid={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("peerid=%2D%41%41%2D%61%61%61%61%61%61%61%61%61%61%61%61%61%61%61%61");
    }

    @Test
    public void shouldNotUrlEncodePeerId() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                PeerIdGeneratorTest.createForPattern("-AA-[a]{16}", false),
                defaultKeyGenerator,
                new UrlEncoder("", Casing.UPPER),
                "peerid={peerid}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("peerid=-AA-aaaaaaaaaaaaaaaa");
    }

    @Test
    public void shouldReplaceEvent() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "event={event}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("event=STARTED");
        // If none, event must be deleted
        assertThat(client.createRequestQuery(RequestEvent.NONE, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("");
        assertThat(client.createRequestQuery(RequestEvent.STOPPED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("event=STOPPED");
    }

    @Test
    public void shouldReplaceKey() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

        final KeyGenerator keyGenerator = KeyGeneratorTest.createDefault();
        final UrlEncoder urlEncoder = new UrlEncoder("", Casing.UPPER);
        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                keyGenerator,
                urlEncoder,
                "key={key}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("key=" + urlEncoder.encode(keyGenerator.getKey(new InfoHash("a".getBytes()), RequestEvent.STARTED)));
    }

    @Test
    public void shouldFailToBuildIfQueryContainsKeyButBitTorrentClientDoesNot() {
        final ConnectionHandler connectionHandler = ConnectionHandlerTest.createMockedIpv4(12345);
        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne(5468);

        final BitTorrentClient client = new BitTorrentClient(
                defaultPeerIdGenerator,
                defaultKeyGenerator,
                defaultUrlEncoder,
                "uploaded={uploaded}&event={event}&uploaded={uploaded}",
                Collections.emptyList(),
                defaultNumwantProvider
        );

        assertThat(client.createRequestQuery(RequestEvent.STARTED, new InfoHash("a".getBytes()), stats, connectionHandler))
                .isEqualToIgnoringCase("uploaded=5468&event=STARTED&uploaded=5468");
    }

    @Test
    public void shouldFailsIfHeadersContainsRemainingPlaceHolder() {
        final List<HttpHeader> headers = new ArrayList<>();
        headers.add(new HttpHeader("qmqm", "{aohdksdf}"));

        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", headers, defaultNumwantProvider);

        assertThatThrownBy(client::createRequestHeaders)
                .isInstanceOf(UnrecognizedClientPlaceholder.class);
    }

    @Test
    public void shouldNotBuildIfPeerIdIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(null, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("peerIdGenerator cannot be null or empty");
    }

    @Test
    public void shouldBuildAndReturnOptionalEmptyIfKeyIsNull() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, null, defaultUrlEncoder, "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);

        assertThat(client.getKey(mock(InfoHash.class), RequestEvent.STARTED)).isEmpty();
    }

    @Test
    public void shouldNotBuildIfUrlEncoderIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, null, "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("urlEncoder cannot be null");
    }

    @Test
    public void shouldNotBuildIfQueryIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, null, Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildIfQueryIsEmpty() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "     ", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query cannot be null or empty");
    }

    @Test
    public void shouldNotBuildWithoutNumwantProvider() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("numwantProvider cannot be null");
    }

    @Test
    public void shouldNotBuildIfHeadersIsNull() {
        assertThatThrownBy(() -> new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", null, defaultNumwantProvider))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers cannot be null");
    }

    @Test
    public void shouldBuildIfHeadersIsEmpty() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", Collections.emptyList(), defaultNumwantProvider);

        assertThat(client.getHeaders()).isEmpty();
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldBuild() {
        final BitTorrentClient client = new BitTorrentClient(defaultPeerIdGenerator, defaultKeyGenerator, defaultUrlEncoder, "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);
        assertThat(client.getPeerId(mock(InfoHash.class), RequestEvent.STARTED)).isEqualTo(defaultPeerIdGenerator.getPeerId(mock(InfoHash.class), RequestEvent.STARTED));
        assertThat(client.getKey(mock(InfoHash.class), RequestEvent.STARTED).get()).isEqualTo(defaultKeyGenerator.getKey(mock(InfoHash.class), RequestEvent.STARTED));
        assertThat(client.getQuery()).isEqualTo("myqueryString");
        assertThat(client.getHeaders()).hasSize(1);
        assertThat(client.getNumwant(RequestEvent.STARTED)).isEqualTo(defaultNumwantProvider.get(RequestEvent.STARTED));
    }

    @Test
    public void shouldBeEqualsByProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), UrlEncoderTest.createDefault(), "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), UrlEncoderTest.createDefault(), "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);
        assertThat(client).isEqualTo(client2);
    }

    @Test
    public void shouldHaveSameHashCodeWithSameProperties() {
        final BitTorrentClient client = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), UrlEncoderTest.createDefault(), "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);
        final BitTorrentClient client2 = new BitTorrentClient(PeerIdGeneratorTest.createDefault(), KeyGeneratorTest.createDefault(), UrlEncoderTest.createDefault(), "myqueryString", Collections.singletonList(new HttpHeader("Connection", "close")), defaultNumwantProvider);
        assertThat(client.hashCode()).isEqualTo(client2.hashCode());
    }

    @Test
    public void shouldGenerateAnnounceURLAndHeaders() {
        BitTorrentClientProvider provider = new BitTorrentClientProvider(mock(JoalConfigProvider.class), mock(ObjectMapper.class), mock(SeedManager.JoalFoldersPath.class));
        FileUtils.listFiles(clientsPath.toFile(), TrueFileFilter.TRUE, null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = provider.createClient(clientConfig);

                        final ConnectionHandler connHandler = ConnectionHandlerTest.createMockedIpv4(1111);
                        final TorrentSeedStats stats = TorrentSeedStatsTest.createOne();

                        client.createRequestQuery(RequestEvent.STARTED, new InfoHash("adb".getBytes()), stats, connHandler);
                        client.createRequestHeaders();
                    } catch (final Exception e) {
                        fail("Exception for client file [" + file.getName() + "]", e);
                    }
                });
    }

    @Test
    public void shouldBeDeserializable() {
        BitTorrentClientProvider provider = new BitTorrentClientProvider(mock(JoalConfigProvider.class), mock(ObjectMapper.class), mock(SeedManager.JoalFoldersPath.class));
        FileUtils.listFiles(clientsPath.toFile(), TrueFileFilter.TRUE, null)
                .forEach(file -> {
                    try {
                        final String json = new String(Files.readAllBytes(file.toPath()));
                        final BitTorrentClientConfig clientConfig = mapper.readValue(json, BitTorrentClientConfig.class);
                        final BitTorrentClient client = provider.createClient(clientConfig);


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
