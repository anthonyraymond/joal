package org.araymond.joal.web.messages.outgoing.impl.config;

import lombok.Getter;
import org.araymond.joal.web.messages.outgoing.MessagePayload;

/**
 * Created by raymo on 08/07/2017.
 */
@Getter
public class InvalidConfigPayload implements MessagePayload {
    private final String error;

    public InvalidConfigPayload(final Exception e) {
        this.error = e.getMessage();
    }
}
