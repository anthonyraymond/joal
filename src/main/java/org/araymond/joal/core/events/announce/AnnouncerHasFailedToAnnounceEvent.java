package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.util.Collection;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasFailedToAnnounceEvent extends AnnouncerEvent {
    private final String error;

    public AnnouncerHasFailedToAnnounceEvent(final Announcer announcer, final String error) {
        super(announcer);
        Preconditions.checkArgument(!StringUtils.isBlank(error), "Error message cannot be null or empty.");

        this.error = error;
    }

    public String getError() {
        return error;
    }
}
