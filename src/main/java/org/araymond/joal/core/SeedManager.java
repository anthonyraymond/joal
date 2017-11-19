package org.araymond.joal.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.client.emulated.BitTorrentClientProvider;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.filechange.FailedToAddTorrentFileEvent;
import org.araymond.joal.core.events.global.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.global.SeedSessionHasStartedEvent;
import org.araymond.joal.core.torrent.watcher.TorrentFileProvider;
import org.araymond.joal.core.ttorent.client.Client;
import org.araymond.joal.core.ttorent.client.ConnectionHandler;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Created by raymo on 27/01/2017.
 */
public class SeedManager {

    private static final Logger logger = LoggerFactory.getLogger(SeedManager.class);

    private final JoalFoldersPath joalFoldersPath;
    private final JoalConfigProvider configProvider;
    private final TorrentFileProvider torrentFileProvider;
    private final BitTorrentClientProvider bitTorrentClientProvider;
    private final ApplicationEventPublisher publisher;

    private final ConnectionHandler connectionHandler;
    private Client currentClient;

    public void init() throws IOException {
        this.connectionHandler.init();
        this.torrentFileProvider.start();
    }

    public void tearDown() throws IOException {
        this.connectionHandler.close();
        this.currentClient.stop();
    }

    public SeedManager(final String joalConfFolder, final ObjectMapper mapper, final ApplicationEventPublisher publisher) throws IOException {
        this.joalFoldersPath = new JoalFoldersPath(Paths.get(joalConfFolder));
        this.torrentFileProvider = new TorrentFileProvider(joalFoldersPath);
        this.configProvider = new JoalConfigProvider(mapper, joalFoldersPath, publisher);
        this.bitTorrentClientProvider = new BitTorrentClientProvider(configProvider, mapper, joalFoldersPath, publisher);
        this.publisher = publisher;
        this.connectionHandler = new ConnectionHandler();
    }

    public void startSeeding() throws IOException {
        this.configProvider.init();
        this.bitTorrentClientProvider.init();
        this.bitTorrentClientProvider.generateNewClient();
        final BitTorrentClient bitTorrentClient = bitTorrentClientProvider.get();

        this.currentClient = new Client(
                this.connectionHandler,
                configProvider,
                torrentFileProvider,
                publisher,
                bitTorrentClient
        );

        this.currentClient.share();

        publisher.publishEvent(new SeedSessionHasStartedEvent(bitTorrentClient));
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

    public void deleteTorrent(final String torrentInfoHash) {
        this.torrentFileProvider.moveToArchiveFolder(torrentInfoHash);
    }

    public void stop() {
        if (currentClient != null) {
            this.currentClient.stop();
            this.publisher.publishEvent(new SeedSessionHasEndedEvent());
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

}
