package org.araymond.joal.web.messages.outgoing.impl.config;

import lombok.Getter;
import org.araymond.joal.core.config.AppConfiguration;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 08/07/2017.
 */
@Getter
public class ConfigHasBeenLoadedPayload implements MessagePayload {
    private final AppConfiguration config;

    public ConfigHasBeenLoadedPayload(final ConfigHasBeenLoadedEvent event) {
        this.config = event.getConfiguration();
    }
}
