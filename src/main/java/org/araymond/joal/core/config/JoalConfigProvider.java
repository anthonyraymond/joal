package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.core.events.config.ConfigHasChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import javax.inject.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by raymo on 18/04/2017.
 */
public class JoalConfigProvider implements Provider<AppConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(JoalConfigProvider.class);
    private static final String CONF_FILE_NAME = "config.json";

    private final Path joalConfPath;
    private final ObjectMapper objectMapper;
    private AppConfiguration config = null;
    private final ApplicationEventPublisher publisher;

    public JoalConfigProvider(final ObjectMapper objectMapper, final SeedManager.JoalFoldersPath joalFoldersPath, final ApplicationEventPublisher publisher) throws FileNotFoundException {
        this.objectMapper = objectMapper;
        this.publisher = publisher;

        this.joalConfPath = joalFoldersPath.getConfPath().resolve(CONF_FILE_NAME);
        if (!Files.exists(joalConfPath)) {
            throw new FileNotFoundException(String.format("App configuration file '%s' not found.", joalConfPath));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("App configuration file will be searched for in {}", joalConfPath.toAbsolutePath());
        }
    }

    public void init() {
        this.config = this.loadConfiguration();
    }

    @Override
    public AppConfiguration get() {
        if (this.config == null) {
            logger.error("App configuration has not been loaded yet.");
            throw new IllegalStateException("Attempted to get configuration before init.");
        }
        return this.config;
    }

    @VisibleForTesting
    AppConfiguration loadConfiguration() {
        final AppConfiguration configuration;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading json configuration from '{}'.", joalConfPath.toAbsolutePath());
            }
            configuration = objectMapper.readValue(joalConfPath.toFile(), AppConfiguration.class);
            logger.debug("Successfully red json configuration.");
        } catch (final IOException e) {
            logger.error("Failed to read configuration file", e);
            throw new IllegalStateException(e);
        }
        logger.info("App configuration has been successfully loaded.");
        this.publisher.publishEvent(new ConfigHasBeenLoadedEvent(configuration));
        return configuration;
    }

    public void saveNewConf(final AppConfiguration conf) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(joalConfPath.toFile(), conf);
            publisher.publishEvent(new ConfigHasChangedEvent(conf));
        } catch (final IOException e) {
            logger.error("Failed to write new configuration file", e);
            throw new IllegalStateException(e);
        }
    }

}
