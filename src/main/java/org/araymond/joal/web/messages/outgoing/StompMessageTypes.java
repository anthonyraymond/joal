package org.araymond.joal.web.messages.outgoing;

import org.araymond.joal.web.messages.outgoing.impl.announce.*;
import org.araymond.joal.web.messages.outgoing.impl.config.ClientFilesDiscoveredPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasBeenLoadedPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasChangedPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.FailedToAddTorrentFilePayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileAddedPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileDeletedPayload;
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
    ANNOUNCER_HAS_FAILED_TO_ANNOUNCE(AnnouncerHasFailedToAnnouncePayload.class),
    CLIENT_FILES_DISCOVERED(ClientFilesDiscoveredPayload.class),
    CONFIG_HAS_CHANGED(ConfigHasChangedPayload.class),
    INVALID_CONFIG(InvalidConfigPayload.class),
    CONFIG_HAS_BEEN_LOADED(ConfigHasBeenLoadedPayload.class),
    TORRENT_FILE_ADDED(TorrentFileAddedPayload.class),
    TORRENT_FILE_DELETED(TorrentFileDeletedPayload.class),
    FAILED_TO_ADD_TORRENT_FILE(FailedToAddTorrentFilePayload.class);

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
