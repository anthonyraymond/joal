package org.araymond.joal.core.ttorrent.client.announcer;

import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;

public class AnnouncerFactory {
    private final AnnounceDataAccessor announceDataAccessor;

    public AnnouncerFactory(final AnnounceDataAccessor announceDataAccessor) {
        this.announceDataAccessor = announceDataAccessor;
    }

    public Announcer create(final MockedTorrent torrent) {
        return new Announcer(torrent, this.announceDataAccessor);
    }

}
