package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.MockedTorrent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;
import org.araymond.joal.web.messages.outgoing.OutgoingMessage;
import org.araymond.joal.web.messages.outgoing.OutgoingMessageTypes;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnounceMessage extends AnnounceMessage {

    private final String error;

    public AnnouncerHasFailedToAnnounceMessage(final TorrentWithStats torrent, final String error) {
        super(OutgoingMessageTypes.ANNOUNCER_HAS_FAILED_TO_ANNOUNCE, torrent);
        Preconditions.checkArgument(!StringUtils.isBlank(error), "Error message must not be null or empty.");

        this.error = error;
    }

    public String getError() {
        return error;
    }
}
