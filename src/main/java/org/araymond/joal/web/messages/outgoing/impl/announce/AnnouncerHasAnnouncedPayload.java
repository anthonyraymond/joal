package org.araymond.joal.web.messages.outgoing.impl.announce;

import org.araymond.joal.core.events.announce.AnnouncerHasAnnouncedEvent;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.util.Collection;

/**
 * Created by raymo on 25/06/2017.
 */
public class AnnouncerHasAnnouncedPayload extends AnnouncePayload {

    private final int interval;
    private final int seeders;
    private final int leechers;

    public AnnouncerHasAnnouncedPayload(final AnnouncerHasAnnouncedEvent event) {
        super(event);
        this.interval = event.getInterval();
        this.seeders = event.getSeeders();
        this.leechers = event.getLeechers();
    }

    public int getInterval() {
        return interval;
    }

    public int getSeeders() {
        return seeders;
    }

    public int getLeechers() {
        return leechers;
    }
}
