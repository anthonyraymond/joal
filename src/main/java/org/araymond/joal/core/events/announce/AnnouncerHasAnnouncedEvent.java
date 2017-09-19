package org.araymond.joal.core.events.announce;

import org.araymond.joal.core.ttorent.client.announce.Announcer;

/**
 * Created by raymo on 22/05/2017.
 */
public class AnnouncerHasAnnouncedEvent extends AnnouncerEvent {
    private final int interval;
    private final int seeders;
    private final int leechers;

    public AnnouncerHasAnnouncedEvent(final Announcer announcer) {
        super(announcer);

        this.interval = announcer.getSeedingTorrent().getInterval();
        this.seeders = announcer.getSeedingTorrent().getSeeders();
        this.leechers = announcer.getSeedingTorrent().getLeechers();
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
