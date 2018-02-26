package org.araymond.joal.web.messages.outgoing.impl.config;

import org.araymond.joal.core.config.AppConfigurationIntegrityException;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 08/07/2017.
 */
public class InvalidConfigPayload implements MessagePayload {
    private final String error;

    public InvalidConfigPayload(final Exception e) {
        this.error = e.getMessage();
    }

    public String getError() {
        return error;
    }
}
