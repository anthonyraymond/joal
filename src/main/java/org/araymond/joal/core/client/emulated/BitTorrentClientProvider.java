package org.araymond.joal.core.client.emulated;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.araymond.joal.core.config.JoalConfigProvider;
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
            throw new IllegalStateException("Attempt to get a client before it was generated.");
        }
        return bitTorrentClient;
    }

    public BitTorrentClient generateNewClient() throws FileNotFoundException {
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

        return this.bitTorrentClient;
    }
}
