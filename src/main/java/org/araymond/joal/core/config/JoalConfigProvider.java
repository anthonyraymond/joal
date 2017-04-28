package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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
 * Created by raymo on 18/04/2017.
 */
@Component
public class JoalConfigProvider implements Provider<AppConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(JoalConfigProvider.class);
    private static final String CONF_FILE_NAME = "config.json";

    private final Path joalConfPath;
    private final ObjectMapper objectMapper;
    private boolean isDirty = true;
    private AppConfiguration config = null;

    @Inject
    public JoalConfigProvider(final ObjectMapper objectMapper, @Value("${joal-conf}") final String confFolder) throws FileNotFoundException {
        this.objectMapper = objectMapper;

        if (StringUtils.isBlank(confFolder)) {
            throw new IllegalArgumentException("A config path is required.");
        }
        this.joalConfPath = Paths.get(confFolder).resolve(CONF_FILE_NAME);
        if (!Files.exists(joalConfPath)) {
            throw new FileNotFoundException(String.format("Configuration file '%s' not found.", joalConfPath));
        }
    }

    @Override
    public AppConfiguration get() {
        if (this.isDirty || this.config == null) {

            this.config = loadConfiguration();
        }
        return this.config;
    }

    // TODO: implement a watcher to check if config is updated (and then set isDirty)
    void setDirtyState() {
        this.isDirty = true;
    }


    AppConfiguration loadConfiguration() {
        final AppConfiguration configuration;
        try {
            configuration = objectMapper.readValue(joalConfPath.toFile(), AppConfiguration.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        this.isDirty = false;
        return configuration;
    }

}
