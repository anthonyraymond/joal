package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.events.config.ConfigurationIsInDirtyState;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 09/07/2017.
 */
public class ConfigHasChangedPayload implements MessagePayload {

    private final AppConfiguration config;

    public ConfigHasChangedPayload(final ConfigurationIsInDirtyState event) {
        this.config = event.getConfiguration();
    }

    public AppConfiguration getConfig() {
        return config;
    }
}
