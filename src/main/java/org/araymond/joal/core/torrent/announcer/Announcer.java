package org.araymond.joal.core.torrent.announcer;

import com.google.common.base.Objects;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceResponseMessage;
import org.araymond.joal.core.torrent.announcer.tracker.NewTrackerClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Announcer {
    private static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    private int lastKnownInterval = 15;
    private final MockedTorrent torrent;
    private final NewTrackerClient trackerClient;

    public Announcer(final MockedTorrent torrent) {
        this.torrent = torrent;
        this.trackerClient = new NewTrackerClient(torrent);
    }

    public SuccessAnnounceResponse announce(final String requestQuery, final Iterable<Map.Entry<String, String>> headers) throws AnnounceException {
        final SuccessAnnounceResponse responseMessage = this.trackerClient.announce(requestQuery, headers);

        this.lastKnownInterval = responseMessage.getInterval();

        return responseMessage;
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

    public int getLastKnownInterval() {
        return lastKnownInterval;
    }

    public boolean isSeedingTorrent(final InfoHash infoHash) {
        return this.torrent.getTorrentInfoHash().equals(infoHash);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Announcer announcer = (Announcer) o;
        return Objects.equal(torrent.getHexInfoHash(), announcer.torrent.getHexInfoHash());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(torrent.getHexInfoHash());
    }
}
