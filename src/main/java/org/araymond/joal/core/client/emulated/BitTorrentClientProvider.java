package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public BitTorrentClientProvider(final JoalConfigProvider configProvider, final ObjectMapper objectMapper, final SeedManager.JoalFoldersPath joalFoldersPath) {
        this.configProvider = configProvider;
        this.objectMapper = objectMapper;
        this.clientsFolderPath = joalFoldersPath.getClientsFilesPath();
    }

    public List<String> listClientFiles() {
        try (Stream<Path> paths = Files.walk(this.clientsFolderPath)) {
            return paths.filter(p -> p.toString().endsWith(".client"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
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

    public void generateNewClient() throws FileNotFoundException, IllegalStateException {
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
