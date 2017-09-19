package org.araymond.joal.core.events.announce;

import com.google.common.base.Preconditions;
import org.araymond.joal.core.ttorent.client.announce.AnnounceResult;
import org.araymond.joal.core.ttorent.client.announce.Announcer;
import org.araymond.joal.core.ttorent.client.bandwidth.TorrentWithStats;

import java.util.Collection;

/**
 * Created by raymo on 03/07/2017.
 */
public class AnnouncerEvent {

    private final TorrentWithStats torrent;
    private final Collection<AnnounceResult> announceHistory;

    public AnnouncerEvent(final Announcer announcer) {
        Preconditions.checkNotNull(announcer, "Announcer must not be null");

        this.torrent = announcer.getSeedingTorrent();
        this.announceHistory = announcer.getAnnounceHistory();
    }

    public TorrentWithStats getTorrent() {
        return torrent;
    }

    public Collection<AnnounceResult> getAnnounceHistory() {
        return announceHistory;
    }
}
