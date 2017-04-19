package org.araymond.joal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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
    private static final String CONF_FILE_NAME = "config.json";


    private final String joalConfFolder;
    private final ObjectMapper objectMapper;
    private boolean isDirty = true;
    private AppConfiguration config = null;

    @Inject
    public JoalConfigProvider(final ObjectMapper objectMapper, @Value("${joal-conf}") final String confFolder) {
        this.objectMapper = objectMapper;
        this.joalConfFolder = confFolder;
    }

    @Override
    public AppConfiguration get() {
        if (this.isDirty || this.config == null) {
            this.config = loadConfiguration();
        }
        return this.config;
    }

    // TODO: implement a watcher to check if config is updated (and then set isDirty)

    AppConfiguration loadConfiguration() {
        if (StringUtils.isBlank(joalConfFolder)) {
            throw new IllegalArgumentException("A config path is required.");
        }
        final Path joalConfPath = Paths.get(joalConfFolder).resolve(CONF_FILE_NAME);
        if (!Files.exists(joalConfPath)) {
            throw new IllegalStateException(
                    String.format("Configuration file '%s' not found.", joalConfPath),
                    new FileNotFoundException(String.format("Configuration file '%s' not found.", joalConfPath))
            );
        }

        final AppConfiguration configuration;
        try {
            configuration = objectMapper.readValue(joalConfPath.toFile(), AppConfiguration.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        configuration.validate();
        this.isDirty = false;
        return configuration;
    }

}
