package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.core.events.config.ConfigurationIsInDirtyStateEvent;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.isRegularFile;

/**
 * Created by raymo on 18/04/2017.
 */
@Slf4j
public class JoalConfigProvider implements Provider<AppConfiguration> {
    private static final String CONF_FILE_NAME = "config.json";

    private final Path joalConfPath;
    private final ObjectMapper objectMapper;
    private AppConfiguration config = null;
    private final ApplicationEventPublisher publisher;

    public JoalConfigProvider(final ObjectMapper objectMapper, final SeedManager.JoalFoldersPath joalFoldersPath,
                              final ApplicationEventPublisher publisher) throws FileNotFoundException {
        this.objectMapper = objectMapper;
        this.publisher = publisher;

        this.joalConfPath = joalFoldersPath.getConfPath().resolve(CONF_FILE_NAME);
        if (!isRegularFile(joalConfPath)) {
            throw new FileNotFoundException(String.format("App configuration file [%s] not found", joalConfPath));
        }

        log.debug("App configuration file will be searched for in [{}]", joalConfPath.toAbsolutePath());
    }

    public void init() {
        this.config = this.loadConfiguration();
    }

    @Override
    public AppConfiguration get() {
        if (this.config == null) {
            log.error("App configuration has not been loaded yet");
            throw new IllegalStateException("Attempted to get configuration before init");
        }
        return this.config;
    }

    @VisibleForTesting
    AppConfiguration loadConfiguration() {
        final AppConfiguration configuration;
        try {
            log.debug("Reading json configuration from [{}]", joalConfPath.toAbsolutePath());
            configuration = objectMapper.readValue(joalConfPath.toFile(), AppConfiguration.class);
            log.debug("Successfully read json configuration");
        } catch (final IOException e) {
            log.error("Failed to read configuration file", e);
            throw new IllegalStateException(e);
        }

        log.info("App configuration has been successfully loaded");
        this.publisher.publishEvent(new ConfigHasBeenLoadedEvent(configuration));
        return configuration;
    }

    public void saveNewConf(final AppConfiguration conf) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(joalConfPath.toFile(), conf);
            publisher.publishEvent(new ConfigurationIsInDirtyStateEvent(conf));
        } catch (final IOException e) {
            log.error("Failed to write new configuration file", e);
            throw new IllegalStateException(e);
        }
    }
}
