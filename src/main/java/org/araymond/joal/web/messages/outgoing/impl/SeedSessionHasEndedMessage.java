package org.araymond.joal.web.messages.outgoing.impl;

import org.araymond.joal.web.messages.outgoing.OutgoingMessage;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;

/**
 * Created by raymo on 24/06/2017.
 */
public class SeedSessionHasEndedMessage extends OutgoingMessage {

    public SeedSessionHasEndedMessage() {
        super(OutgoingMessageTypes.SEED_SESSION_HAS_ENDED);
    }

}
