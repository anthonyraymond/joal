package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.araymond.joal.core.events.config.ClientFilesDiscoveredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by raymo on 23/04/2017.
 */
public class BitTorrentClientProvider implements Provider<BitTorrentClient> {
    private static final Logger logger = LoggerFactory.getLogger(BitTorrentClientProvider.class);

    private BitTorrentClient bitTorrentClient;
    private final JoalConfigProvider configProvider;
    private final ObjectMapper objectMapper;
    private final Path clientsFolderPath;
    private final ApplicationEventPublisher publisher;

    public BitTorrentClientProvider(final JoalConfigProvider configProvider, final ObjectMapper objectMapper, final SeedManager.JoalFoldersPath joalFoldersPath, final ApplicationEventPublisher publisher) {
        this.configProvider = configProvider;
        this.objectMapper = objectMapper;
        this.clientsFolderPath = joalFoldersPath.getClientsFilesPath();
        this.publisher = publisher;
    }

    public void init() {
        try (Stream<Path> paths = Files.walk(this.clientsFolderPath)) {
            final List<String> clients = paths.filter(p -> p.toString().endsWith(".client"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
            publisher.publishEvent(new ClientFilesDiscoveredEvent(clients));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to walk through .clients files", e);
        }
    }

    @Override
    public BitTorrentClient get() {
        if (bitTorrentClient == null) {
            try {
                generateNewClient();
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return bitTorrentClient;
    }

    public void generateNewClient() throws FileNotFoundException {
        logger.debug("Generating new client.");
        final Path clientConfigPath = clientsFolderPath.resolve(configProvider.get().getClientFileName());
        if (!Files.exists(clientConfigPath)) {
            throw new FileNotFoundException(String.format("BitTorrent client configuration file '%s' not found.", clientConfigPath.toAbsolutePath()));
        }

        final BitTorrentClientConfig config;
        try {
            config = objectMapper.readValue(clientConfigPath.toFile(), BitTorrentClientConfig.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        this.bitTorrentClient = config.createClient();
        logger.debug("New client successfully generated.");
    }
}
