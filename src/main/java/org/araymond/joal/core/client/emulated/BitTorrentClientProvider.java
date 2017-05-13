package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.config.JoalConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by raymo on 23/04/2017.
 */
@Component
public class BitTorrentClientProvider implements Provider<BitTorrentClient> {
    private static final Logger logger = LoggerFactory.getLogger(BitTorrentClientProvider.class);

    private BitTorrentClient bitTorrentClient;
    private final JoalConfigProvider configProvider;
    private final ObjectMapper objectMapper;
    private final Path clientsFolderPath;

    @Inject
    public BitTorrentClientProvider(final JoalConfigProvider configProvider, final ObjectMapper objectMapper, @Value("${joal-conf}") final String confFolder) {
        this.configProvider = configProvider;
        this.objectMapper = objectMapper;
        this.clientsFolderPath = Paths.get(confFolder).resolve("clients");
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
