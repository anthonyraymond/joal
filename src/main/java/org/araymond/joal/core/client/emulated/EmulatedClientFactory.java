package org.araymond.joal.core.client.emulated;

import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by raymo on 26/01/2017.
 */
public final class EmulatedClientFactory {

    private final TorrentClientConfig emulatedClientConfig;

    private EmulatedClientFactory(final TorrentClientConfig emulatedClientConfig) {
        this.emulatedClientConfig = emulatedClientConfig;
    }

    public static EmulatedClientFactory createFactory(final Path clientConfigFilePath) throws IOException {
        return new EmulatedClientFactory(getClientConfigFromFile(clientConfigFilePath));
    }

    public EmulatedClient createClient() {
        return this.emulatedClientConfig.createClient();
    }

    private static TorrentClientConfig getClientConfigFromFile(final Path clientConfigFilePath) throws IOException {
        if (!Files.exists(clientConfigFilePath)) {
            throw new FileNotFoundException(String.format("Configuration file '%s' not found.", clientConfigFilePath.toString()));
        }

        final TorrentClientConfig config;
        try (Reader reader = new FileReader(clientConfigFilePath.toFile())) {
            config = new GsonBuilder().create().fromJson(reader, TorrentClientConfig.class);
        }
        config.validate();
        return config;
    }

}
