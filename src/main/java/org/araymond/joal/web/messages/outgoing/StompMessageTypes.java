package org.araymond.joal.web.messages.outgoing;

import org.araymond.joal.web.messages.outgoing.impl.announce.FailedToAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.SuccessfullyAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.TooManyAnnouncesFailedPayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.WillAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasBeenLoadedPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigIsInDirtyStatePayload;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ListOfClientFilesPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.FailedToAddTorrentFilePayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileAddedPayload;
import org.araymond.joal.web.messages.outgoing.impl.files.TorrentFileDeletedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStartedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStoppedPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymo on 29/06/2017.
 */
public enum StompMessageTypes {
    //announce
    FAILED_TO_ANNOUNCE(FailedToAnnouncePayload.class),
    SUCCESSFULLY_ANNOUNCE(SuccessfullyAnnouncePayload.class),
    TOO_MANY_ANNOUNCES_FAILED(TooManyAnnouncesFailedPayload.class),
    WILL_ANNOUNCE(WillAnnouncePayload.class),

    //config
    CONFIG_HAS_BEEN_LOADED(ConfigHasBeenLoadedPayload.class),
    CONFIG_IS_IN_DIRTY_STATE(ConfigIsInDirtyStatePayload.class),
    INVALID_CONFIG(InvalidConfigPayload.class),
    LIST_OF_CLIENT_FILES(ListOfClientFilesPayload.class),

    // files
    TORRENT_FILE_ADDED(TorrentFileAddedPayload.class),
    TORRENT_FILE_DELETED(TorrentFileDeletedPayload.class),
    FAILED_TO_ADD_TORRENT_FILE(FailedToAddTorrentFilePayload.class),

    //global.state
    GLOBAL_SEED_STARTED(GlobalSeedStartedPayload.class),
    GLOBAL_SEED_STOPPED(GlobalSeedStoppedPayload.class);

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
