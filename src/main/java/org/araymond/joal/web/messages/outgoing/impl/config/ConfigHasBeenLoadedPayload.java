package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.events.old.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 08/07/2017.
 */
public class ConfigHasBeenLoadedPayload implements MessagePayload {

    private final AppConfiguration config;

    public ConfigHasBeenLoadedPayload(final ConfigHasBeenLoadedEvent event) {
        this.config = event.getConfiguration();
    }

    public AppConfiguration getConfig() {
        return config;
    }
}
