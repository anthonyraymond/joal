package org.araymond.joal.core.events.config;

import org.araymond.joal.core.config.AppConfiguration;

/**
 * Created by raymo on 09/07/2017.
 */
public class ConfigHasChangedEvent {
    private final AppConfiguration configuration;

    public ConfigHasChangedEvent(final AppConfiguration configuration) {
        this.configuration = configuration;
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }
}
