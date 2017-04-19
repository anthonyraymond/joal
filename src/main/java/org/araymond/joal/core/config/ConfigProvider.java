package org.araymond.joal.core.config;

import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by raymo on 24/01/2017.
 */
public class ConfigProvider {

    private static AppConfiguration config = null;

    public static void init(final Path configPath) throws IOException {
        if (!Files.exists(configPath)) {
            throw new FileNotFoundException(String.format("Configuration file '%s' not found.", configPath));
        }

        try (Reader reader = new FileReader(configPath.toFile())) {
            config = new GsonBuilder().create().fromJson(reader, AppConfiguration.class);
        }
        config.validate();
    }

    public static AppConfiguration get() {
        if (config != null) {
            return config;
        }
        throw new IllegalStateException("Configuration have not been loaded on application startup.");
    }

}
