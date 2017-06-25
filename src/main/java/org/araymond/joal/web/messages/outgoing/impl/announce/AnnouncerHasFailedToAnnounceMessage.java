package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.web.messages.outgoing.OutgoingMessage;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnounceMessage extends OutgoingMessage {

    private final MockedTorrent torrent;
    private final String error;

    protected AnnouncerHasFailedToAnnounceMessage(final MockedTorrent torrent, final String error) {
        super(OutgoingMessageTypes.ANNOUNCER_HAS_FAILED_TO_ANNOUNCE);
        Preconditions.checkNotNull(torrent, "Torrent must not be null.");
        Preconditions.checkArgument(!StringUtils.isBlank(error), "Error message must not be null or empty.");

        this.torrent = torrent;
        this.error = error;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

    public String getError() {
        return error;
    }
}
