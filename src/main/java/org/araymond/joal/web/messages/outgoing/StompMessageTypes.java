package org.araymond.joal.web.messages.outgoing;

import org.araymond.joal.web.messages.outgoing.impl.announce.*;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasEndedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasStartedPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 29/06/2017.
 */
public enum StompMessageTypes {
    SEED_SESSION_HAS_STARTED(SeedSessionHasStartedPayload.class),
    SEED_SESSION_HAS_ENDED(SeedSessionHasEndedPayload.class),
    ANNOUNCER_HAS_STARTED(AnnouncerHasStartedPayload.class),
    ANNOUNCER_HAS_STOPPED(AnnouncerHasStoppedPayload.class),
    ANNOUNCER_WILL_ANNOUNCE(AnnouncerWillAnnouncePayload.class),
    ANNOUNCER_HAS_ANNOUNCED(AnnouncerHasAnnouncedPayload.class),
    ANNOUNCER_HAS_FAILED_TO_ANNOUNCE(AnnouncerHasFailedToAnnouncePayload.class);

    private static final Map<Class<? extends MessagePayload>, StompMessageTypes> classToType = new HashMap<>();
    private final Class<? extends MessagePayload> clazz;

    static {
        for (final StompMessageTypes type : StompMessageTypes.values()) {
            classToType.put(type.clazz, type);
        }
    }

    StompMessageTypes(final Class<? extends MessagePayload> clazz) {
        this.clazz = clazz;
    }

    static StompMessageTypes typeFor(final MessagePayload payload) {
        return typeFor(payload.getClass());
    }
    static StompMessageTypes typeFor(final Class<? extends MessagePayload> clazz) {
        final StompMessageTypes type = classToType.get(clazz);
        if (type == null) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " is not mapped with a StompMessageType.");
        }
        return type;
    }
}
