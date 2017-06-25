package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.web.messages.outgoing.OutgoingMessage;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedMessage extends OutgoingMessage {

    private final MockedTorrent torrent;

    protected AnnouncerHasAnnouncedMessage(final MockedTorrent torrent) {
        super(OutgoingMessageTypes.ANNOUNCER_HAS_ANNOUNCED);
        Preconditions.checkNotNull(torrent, "Torrent must not be null.");

        this.torrent = torrent;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }
}
