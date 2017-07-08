package org.araymond.joal.core.events.config;

import org.araymond.joal.core.config.AppConfiguration;
import org.springframework.context.ApplicationEvent;

/**
 * Created by raymo on 08/07/2017.
 */
public class ConfigHasBeenLoadedEvent {
    private final AppConfiguration configuration;

    public ConfigHasBeenLoadedEvent(final AppConfiguration configuration) {
        this.configuration = configuration;
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }
}
