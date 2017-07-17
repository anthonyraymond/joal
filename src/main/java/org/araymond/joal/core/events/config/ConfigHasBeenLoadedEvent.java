package org.araymond.joal.core.events.config;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.config.AppConfiguration;

/**
 * Created by raymo on 08/07/2017.
 */
public class ConfigHasBeenLoadedEvent {
    private final AppConfiguration configuration;

    public ConfigHasBeenLoadedEvent(final AppConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "Configuration must not be null.");
        this.configuration = configuration;
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }
}
