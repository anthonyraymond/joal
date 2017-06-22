package org.araymond.joal.web.messages.outgoing.impl;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.web.messages.outgoing.OutgoingMessage;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;

/**
 * Created by raymo on 22/06/2017.
 */
public class ClientHasStartedMessage extends OutgoingMessage {

    private final String client;

    public ClientHasStartedMessage(final String client) {
        super(OutgoingMessageTypes.CLIENT_HAS_STARTED);
        Preconditions.checkArgument(!StringUtils.isBlank(client), "Client must not be null or empty.");

        this.client = client;
    }

    public String getClient() {
        return client;
    }
}
