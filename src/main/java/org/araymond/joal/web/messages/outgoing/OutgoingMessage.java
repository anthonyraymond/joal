package org.araymond.joal.web.messages.outgoing;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by raymo on 22/06/2017.
 */
public abstract class OutgoingMessage {

    private final OutgoingMessageTypes type;

    protected OutgoingMessage(final OutgoingMessageTypes type) {
        Preconditions.checkNotNull(type, "Type must not be null or empty.");
        this.type = type;
    }

    public OutgoingMessageTypes getType() {
        return type;
    }
}
