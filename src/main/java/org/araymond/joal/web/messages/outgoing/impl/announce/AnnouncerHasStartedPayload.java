package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerHasStartedEvent;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasStartedPayload extends AnnouncePayload {

    public AnnouncerHasStartedPayload(final AnnouncerHasStartedEvent event) {
        super(event);
    }
}
