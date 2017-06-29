package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnouncePayload extends AnnouncePayload {

    private final String error;

    public AnnouncerHasFailedToAnnouncePayload(final TorrentWithStats torrent, final String error) {
        super(torrent);
        Preconditions.checkArgument(!StringUtils.isBlank(error), "Error message must not be null or empty.");

        this.error = error;
    }

    public String getError() {
        return error;
    }
}
