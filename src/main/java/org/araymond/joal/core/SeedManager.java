package org.araymond.joal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by raymo on 27/01/2017.
 */
public class SeedManager {

    private static final Logger logger = LoggerFactory.getLogger(SeedManager.class);
    private final CloseableHttpClient httpClient;
    private boolean isSeeding;
    private final JoalFoldersPath joalFoldersPath;
    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClientProvider bitTorrentClientProvider;
    private final ApplicationEventPublisher publisher;
    private final ConnectionHandler connectionHandler;
    private BandwidthDispatcher bandwidthDispatcher;
    private ClientFacade client;

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

    public SeedManager(final String joalConfFolder, final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        this.isSeeding = false;
        this.joalFoldersPath = new JoalFoldersPath(Paths.get(joalConfFolder));
        this.torrentFileProvider = new TorrentFileProvider(joalFoldersPath);
        this.configProvider = new JoalConfigProvider(mapper, joalFoldersPath, publisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalFoldersPath);
        this.publisher = publisher;
        this.connectionHandler = new ConnectionHandler();


        final SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(30000)
                .build();
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(100);
        connManager.setMaxTotal(200);
        connManager.setValidateAfterInactivity(1000);
        connManager.setDefaultSocketConfig(sc);
        this.httpClient = HttpClients.custom()
                .setConnectionTimeToLive(1, TimeUnit.MINUTES)
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .build();
    }

    public void startSeeding() throws IOException {
        if (this.client != null) {
            return;
        }
        this.isSeeding = true;

        this.configProvider.init();
        final AppConfiguration appConfiguration = this.configProvider.get();
        final List<String> clientFiles = this.bitTorrentClientProvider.listClientFiles();
        this.publisher.publishEvent(new ListOfClientFilesEvent(clientFiles));
        this.bitTorrentClientProvider.generateNewClient();
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();

        final RandomSpeedProvider randomSpeedProvider = new RandomSpeedProvider(appConfiguration);
        this.bandwidthDispatcher = new BandwidthDispatcher(5000, randomSpeedProvider);
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
            // test if torrent file is valid or not.
            MockedTorrent.fromBytes(bytes);

            final String torrentName = name.endsWith(".torrent") ? name : name + ".torrent";
            Files.write(this.joalFoldersPath.getTorrentFilesPath().resolve(torrentName), bytes, StandardOpenOption.CREATE);
        } catch (final Exception e) {
            logger.warn("Failed to save torrent file", e);
            // If NullPointerException occurs (when the file is an empty file) there is no message.
            final String errorMessage = Optional.ofNullable(e.getMessage()).orElse("Empty file");
            this.publisher.publishEvent(new FailedToAddTorrentFileEvent(name, errorMessage));
        }
    }

    public void deleteTorrent(final InfoHash torrentInfoHash) {
        this.torrentFileProvider.moveToArchiveFolder(torrentInfoHash);
    }

    public boolean isSeeding() {
        return this.isSeeding;
    }

    public List<MockedTorrent> getTorrentFiles() {
        return torrentFileProvider.getTorrentFiles();
    }

    public List<String> listClientFiles() {
        return bitTorrentClientProvider.listClientFiles();
    }

    public List<AnnouncerFacade> getCurrentlySeedingAnnouncer() {
        if (this.client == null) {
            return new ArrayList<>();
        }
        return client.getCurrentlySeedingAnnouncer();
    }

    public Map<InfoHash, Speed> getSpeedMap() {
        if (this.bandwidthDispatcher == null) {
            return Maps.newHashMap();
        }
        return bandwidthDispatcher.getSpeedMap();
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
                    .filter(entry -> "User-Agent".equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("Unknown");
        } catch (final IllegalStateException e) {
            return "None";
        }
    }

    public void stop() {
        this.isSeeding = false;
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


    public static class JoalFoldersPath {
        private final Path confPath;
        private final Path torrentFilesPath;
        private final Path torrentArchivedPath;
        private final Path clientsFilesPath;

        public JoalFoldersPath(final Path confPath) {
            this.confPath = confPath;
            this.torrentFilesPath = this.confPath.resolve("torrents");
            this.torrentArchivedPath = this.torrentFilesPath.resolve("archived");
            this.clientsFilesPath = this.confPath.resolve("clients");

            if (!Files.exists(confPath)) {
                logger.warn("No such directory: {}", this.confPath.toString());
            }
            if (!Files.exists(torrentFilesPath)) {
                logger.warn("Sub-folder 'torrents' is missing in joal conf folder: {}", this.torrentFilesPath.toString());
            }
            if (!Files.exists(clientsFilesPath)) {
                logger.warn("Sub-folder 'clients' is missing in joal conf folder: {}", this.clientsFilesPath.toString());
            }
        }

        public Path getConfPath() {
            return confPath;
        }
        public Path getTorrentFilesPath() {
            return torrentFilesPath;
        }
        public Path getTorrentArchivedPath() {
            return torrentArchivedPath;
        }
        public Path getClientsFilesPath() {
            return clientsFilesPath;
        }
    }

    private static final class SeedManagerSpeedChangeListener implements SpeedChangedListener {
        private final ApplicationEventPublisher publisher;

        private SeedManagerSpeedChangeListener(final ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void speedsHasChanged(final Map<InfoHash, Speed> speeds) {
            this.publisher.publishEvent(new SeedingSpeedsHasChangedEvent(speeds));
        }
    }

}
