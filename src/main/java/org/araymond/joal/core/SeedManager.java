package org.araymond.joal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.bandwith.RandomSpeedProvider;
import org.araymond.joal.core.bandwith.Speed;
import org.araymond.joal.core.bandwith.SpeedChangedListener;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig;
import org.araymond.joal.core.client.emulated.BitTorrentClientProvider;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStartedEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStoppedEvent;
import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.core.events.torrent.files.FailedToAddTorrentFileEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorrent.client.ClientBuilder;
import org.araymond.joal.core.ttorrent.client.ClientFacade;
import org.araymond.joal.core.ttorrent.client.ConnectionHandler;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFacade;
import org.araymond.joal.core.ttorrent.client.announcer.AnnouncerFactory;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.isDirectory;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * This is the outer boundary of our the business logic. Most (if not all)
 * torrent-related handling is happening here & downstream.
 */
@Slf4j
public class SeedManager {

    private final CloseableHttpClient httpClient;
    @Getter
    private boolean seeding;
    private final JoalFoldersPath joalFoldersPath;
    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClientProvider bitTorrentClientProvider;
    private final ApplicationEventPublisher publisher;
    private final ConnectionHandler connectionHandler;
    private BandwidthDispatcher bandwidthDispatcher;
    private ClientFacade client;

    public SeedManager(final String joalConfFolder, final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        this.joalFoldersPath = new JoalFoldersPath(Paths.get(joalConfFolder));
        this.torrentFileProvider = new TorrentFileProvider(joalFoldersPath);
        this.configProvider = new JoalConfigProvider(mapper, joalFoldersPath, publisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalFoldersPath);
        this.publisher = publisher;
        this.connectionHandler = new ConnectionHandler();

        final SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(30_000)
                .build();
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(100);
        connManager.setMaxTotal(200);
        connManager.setValidateAfterInactivity(1000);
        connManager.setDefaultSocketConfig(sc);

        RequestConfig requestConf = RequestConfig.custom()
                .setConnectTimeout(10_000)
                .setConnectionRequestTimeout(5000)  // timeout for requesting connection from connection manager
                .setSocketTimeout(5000)
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionTimeToLive(1, TimeUnit.MINUTES)
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(requestConf)
                .build();
    }

    public void init() throws IOException {
        this.connectionHandler.start();
        this.torrentFileProvider.start();
    }

    public void tearDown() {
        this.connectionHandler.close();
        this.torrentFileProvider.stop();
        if (this.client != null) {
            this.client.stop();
        }
    }

    public void startSeeding() throws IOException {
        if (this.client != null) {
            return;
        }
        this.seeding = true;

        this.configProvider.init();
        final AppConfiguration appConfiguration = this.configProvider.get();
        this.publisher.publishEvent(new ListOfClientFilesEvent(this.listClientFiles()));
        this.bitTorrentClientProvider.generateNewClient();
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();

        final RandomSpeedProvider randomSpeedProvider = new RandomSpeedProvider(appConfiguration);
        this.bandwidthDispatcher = new BandwidthDispatcher(5000, randomSpeedProvider);  // TODO: move interval to config
        this.bandwidthDispatcher.setSpeedListener(new SeedManagerSpeedChangeListener(this.publisher));
        this.bandwidthDispatcher.start();

        final AnnounceDataAccessor announceDataAccessor = new AnnounceDataAccessor(bitTorrentClient, bandwidthDispatcher, this.connectionHandler);

        this.client = ClientBuilder.builder()
                .withAppConfiguration(appConfiguration)
                .withTorrentFileProvider(this.torrentFileProvider)
                .withBandwidthDispatcher(this.bandwidthDispatcher)
                .withAnnouncerFactory(new AnnouncerFactory(announceDataAccessor, httpClient))
                .withEventPublisher(this.publisher)
                .withDelayQueue(new DelayQueue<>())
                .build();

        this.client.start();
        publisher.publishEvent(new GlobalSeedStartedEvent(bitTorrentClient));
    }

    public void saveNewConfiguration(final AppConfiguration config) {
        this.configProvider.saveNewConf(config);
    }

    public void saveTorrentToDisk(final String name, final byte[] bytes) {
        try {
            MockedTorrent.fromBytes(bytes);  // test if torrent file is valid or not

            final String torrentName = name.endsWith(".torrent") ? name : name + ".torrent";
            Files.write(this.joalFoldersPath.getTorrentFilesPath().resolve(torrentName), bytes, StandardOpenOption.CREATE);
        } catch (final Exception e) {
            log.warn("Failed to save torrent file", e);
            // If NullPointerException occurs (when the file is an empty file) there is no message.
            final String errorMessage = firstNonNull(e.getMessage(), "Empty/bad file");
            this.publisher.publishEvent(new FailedToAddTorrentFileEvent(name, errorMessage));
        }
    }

    public void deleteTorrent(final InfoHash torrentInfoHash) {
        this.torrentFileProvider.moveToArchiveFolder(torrentInfoHash);
    }

    public List<MockedTorrent> getTorrentFiles() {
        return torrentFileProvider.getTorrentFiles();
    }

    public List<String> listClientFiles() {
        return bitTorrentClientProvider.listClientFiles();
    }

    public List<AnnouncerFacade> getCurrentlySeedingAnnouncers() {
        return this.client == null ? emptyList() : client.getCurrentlySeedingAnnouncers();
    }

    public Map<InfoHash, Speed> getSpeedMap() {
        return this.bandwidthDispatcher == null ? emptyMap() : bandwidthDispatcher.getSpeedMap();
    }

    public AppConfiguration getCurrentConfig() {
        try {
            return this.configProvider.get();
        } catch (final IllegalStateException e) {
            this.configProvider.init();
            return this.configProvider.get();
        }
    }

    public String getCurrentEmulatedClient() {
        try {
            return this.bitTorrentClientProvider.get().getHeaders().stream()
                    .filter(hdr -> USER_AGENT.equalsIgnoreCase(hdr.getName()))
                    .findFirst()
                    .map(BitTorrentClientConfig.HttpHeader::getValue)
                    .orElse("Unknown");
        } catch (final IllegalStateException e) {
            return "None";
        }
    }

    public void stop() {
        this.seeding = false;
        if (client != null) {
            this.client.stop();
            this.publisher.publishEvent(new GlobalSeedStoppedEvent());
            this.client = null;
        }
        if (this.bandwidthDispatcher != null) {
            this.bandwidthDispatcher.stop();
            this.bandwidthDispatcher.setSpeedListener(null);
            this.bandwidthDispatcher = null;
        }
    }


    // TODO: move to config, also rename?
    @Getter
    public static class JoalFoldersPath {
        private final Path confPath;
        private final Path torrentFilesPath;
        private final Path torrentArchivedPath;
        private final Path clientsFilesPath;

        /**
         * Resolves, stores & exposes location to various configuration file-paths.
         */
        public JoalFoldersPath(final Path confPath) {
            this.confPath = confPath;
            this.torrentFilesPath = this.confPath.resolve("torrents");
            this.torrentArchivedPath = this.torrentFilesPath.resolve("archived");
            this.clientsFilesPath = this.confPath.resolve("clients");

            if (!isDirectory(confPath)) {
                log.warn("No such directory: {}", this.confPath);
            }
            if (!isDirectory(torrentFilesPath)) {
                log.warn("Sub-folder 'torrents' is missing in joal conf folder: {}", this.torrentFilesPath);
            }
            if (!isDirectory(clientsFilesPath)) {
                log.warn("Sub-folder 'clients' is missing in joal conf folder: {}", this.clientsFilesPath);
            }
        }
    }

    @RequiredArgsConstructor
    private static final class SeedManagerSpeedChangeListener implements SpeedChangedListener {
        private final ApplicationEventPublisher publisher;

        @Override
        public void speedsHasChanged(final Map<InfoHash, Speed> speeds) {
            this.publisher.publishEvent(new SeedingSpeedsHasChangedEvent(speeds));
        }
    }
}
