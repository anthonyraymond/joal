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
    @Getter private boolean seeding;
    private final JoalFoldersPath joalFoldersPath;
    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClientProvider bitTorrentClientProvider;
    private final ApplicationEventPublisher appEventPublisher;
    private final ConnectionHandler connectionHandler = new ConnectionHandler();
    private BandwidthDispatcher bandwidthDispatcher;
    private ClientFacade client;

    public SeedManager(final String joalConfRootPath, final ObjectMapper mapper,
                       final ApplicationEventPublisher appEventPublisher) throws IOException {
        this.joalFoldersPath = new JoalFoldersPath(Paths.get(joalConfRootPath));
        this.torrentFileProvider = new TorrentFileProvider(joalFoldersPath);
        this.configProvider = new JoalConfigProvider(mapper, joalFoldersPath, appEventPublisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalFoldersPath);
        this.appEventPublisher = appEventPublisher;

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
            this.client = null;
        }
    }

    public void startSeeding() throws IOException {
        if (this.seeding) {
            log.warn("startSeeding() called, but already running");
            return;
        }
        this.seeding = true;

        final AppConfiguration appConfig = this.configProvider.init();
        this.appEventPublisher.publishEvent(new ListOfClientFilesEvent(this.listClientFiles()));
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.generateNewClient();

        this.bandwidthDispatcher = new BandwidthDispatcher(5000, new RandomSpeedProvider(appConfig));  // TODO: move interval to config
        this.bandwidthDispatcher.setSpeedListener(new SeedManagerSpeedChangeListener(this.appEventPublisher));
        this.bandwidthDispatcher.start();

        final AnnounceDataAccessor announceDataAccessor = new AnnounceDataAccessor(bitTorrentClient, bandwidthDispatcher, connectionHandler);

        this.client = ClientBuilder.builder()
                .withAppConfiguration(appConfig)
                .withTorrentFileProvider(this.torrentFileProvider)
                .withBandwidthDispatcher(this.bandwidthDispatcher)
                .withAnnouncerFactory(new AnnouncerFactory(announceDataAccessor, httpClient, appConfig))
                .withEventPublisher(this.appEventPublisher)
                .withDelayQueue(new DelayQueue<>())
                .build();

        this.client.start();
        appEventPublisher.publishEvent(new GlobalSeedStartedEvent(bitTorrentClient));
    }

    public void saveNewConfiguration(final AppConfiguration config) {
        this.configProvider.saveNewConf(config);
    }

    public void saveTorrentToDisk(final String name, final byte[] bytes) {
        try {
            MockedTorrent.fromBytes(bytes);  // test if torrent file is valid or not

            final String torrentName = name.endsWith(".torrent") ? name : name + ".torrent";
            Files.write(this.joalFoldersPath.getTorrentsDirPath().resolve(torrentName), bytes, StandardOpenOption.CREATE);
        } catch (final Exception e) {
            log.warn("Failed to save torrent file", e);
            // If NullPointerException occurs (when the file is an empty file) there is no message.
            final String errorMessage = firstNonNull(e.getMessage(), "Empty/bad file");
            this.appEventPublisher.publishEvent(new FailedToAddTorrentFileEvent(name, errorMessage));
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
            return this.configProvider.init();
        }
    }

    public String getCurrentEmulatedClient() {
        try {
            return this.bitTorrentClientProvider.get().getHeaders().stream()
                    .filter(hdr -> USER_AGENT.equalsIgnoreCase(hdr.getKey()))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse("Unknown");
        } catch (final IllegalStateException e) {
            return "None";
        }
    }

    public void stop() {
        this.seeding = false;
        if (client != null) {
            this.client.stop();
            this.appEventPublisher.publishEvent(new GlobalSeedStoppedEvent());
            this.client = null;
        }
        if (this.bandwidthDispatcher != null) {
            this.bandwidthDispatcher.stop();
            this.bandwidthDispatcher.setSpeedListener(null);
            this.bandwidthDispatcher = null;
        }
    }


    /**
     * Contains the references to all the directories
     * containing settings/configurations/torrent sources
     * for JOAL.
     */
    // TODO: move to config, also rename?
    @Getter
    public static class JoalFoldersPath {
        private final Path confDirRootPath;  // all other directories stem from this
        private final Path torrentsDirPath;
        private final Path torrentArchiveDirPath;
        private final Path clientFilesDirPath;

        /**
         * Resolves, stores & exposes location to various configuration file-paths.
         */
        public JoalFoldersPath(final Path confDirRootPath) {
            this.confDirRootPath = confDirRootPath;
            this.torrentsDirPath = this.confDirRootPath.resolve("torrents");
            this.torrentArchiveDirPath = this.torrentsDirPath.resolve("archived");
            this.clientFilesDirPath = this.confDirRootPath.resolve("clients");

            if (!isDirectory(confDirRootPath)) {
                log.warn("No such directory: [{}]", this.confDirRootPath);
            }
            if (!isDirectory(torrentsDirPath)) {
                log.warn("Sub-folder 'torrents' is missing in joal conf folder: [{}]", this.torrentsDirPath);
            }
            if (!isDirectory(clientFilesDirPath)) {
                log.warn("Sub-folder 'clients' is missing in joal conf folder: [{}]", this.clientFilesDirPath);
            }
        }
    }

    @RequiredArgsConstructor
    private static final class SeedManagerSpeedChangeListener implements SpeedChangedListener {
        private final ApplicationEventPublisher appEventPublisher;

        @Override
        public void speedsHasChanged(final Map<InfoHash, Speed> speeds) {
            this.appEventPublisher.publishEvent(new SeedingSpeedsHasChangedEvent(speeds));
        }
    }
}
