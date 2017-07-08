package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
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
    private boolean isDirty = true;
    private AppConfiguration config = null;
    private final ApplicationEventPublisher publisher;

    public JoalConfigProvider(final ObjectMapper objectMapper, final String confFolder, final ApplicationEventPublisher publisher) throws FileNotFoundException {
        this.objectMapper = objectMapper;
        this.publisher = publisher;

        if (StringUtils.isBlank(confFolder)) {
            throw new IllegalArgumentException("A config path is required.");
        }
        this.joalConfPath = Paths.get(confFolder).resolve(CONF_FILE_NAME);
        if (!Files.exists(joalConfPath)) {
            throw new FileNotFoundException(String.format("App configuration file '%s' not found.", joalConfPath));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("App configuration file will be searched for in {}", joalConfPath.toAbsolutePath());
        }
    }

    @Override
    public AppConfiguration get() {
        if (this.isDirty || this.config == null) {
            logger.info("App configuration is dirty or has not been loaded yet.");
            this.config = this.loadConfiguration();
        }
        return this.config;
    }

    @VisibleForTesting
    void setDirtyState() {
        logger.debug("App configuration has been set to dirty state.");
        this.isDirty = true;
    }

    @VisibleForTesting
    boolean isDirty() {
        return this.isDirty;
    }


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
        this.isDirty = false;
        logger.info("App configuration has been successfully loaded.");
        this.publisher.publishEvent(new ConfigHasBeenLoadedEvent(configuration));
        return configuration;
    }

    public void saveNewConf(final AppConfiguration conf) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(joalConfPath.toFile(), conf);
        } catch (final IOException e) {
            logger.error("Failed to write new configuration file", e);
            throw new IllegalStateException(e);
        }
    }

}
