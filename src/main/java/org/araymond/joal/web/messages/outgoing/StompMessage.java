package org.araymond.joal.web.messages.outgoing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import lombok.Getter;

/**
 * Created by raymo on 29/06/2017.
 */
@Getter
public class StompMessage {

    private final StompMessageTypes type;
    private final MessagePayload payload;

    @VisibleForTesting
    StompMessage(final StompMessageTypes type, final MessagePayload payload) {
        Preconditions.checkNotNull(type, "Type must not be null or empty.");
        Preconditions.checkNotNull(payload, "Payload must not be null or empty.");

        this.type = type;
        this.payload = payload;
    }

    public static StompMessage wrap(final MessagePayload payload) {
        return new StompMessage(StompMessageTypes.typeFor(payload), payload);
    }
}
