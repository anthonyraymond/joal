package org.araymond.joal.web.messages.outgoing.impl.announce;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.core.events.announce.AnnouncerHasFailedToAnnounceEvent;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.util.Collection;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasFailedToAnnouncePayload extends AnnouncePayload {

    private final String error;

    public AnnouncerHasFailedToAnnouncePayload(final AnnouncerHasFailedToAnnounceEvent event) {
        super(event);

        this.error = event.getError();
    }


    public String getError() {
        return error;
    }
}
