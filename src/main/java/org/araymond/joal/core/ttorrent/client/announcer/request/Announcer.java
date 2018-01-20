package org.araymond.joal.core.ttorrent.client.announcer.request;

import com.google.common.base.Objects;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.NewTrackerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Announcer {
    private static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    private int lastKnownInterval = 10;
    private int consecutiveFails = 0;
    private final MockedTorrent torrent;
    private final NewTrackerClient trackerClient;
    private final AnnounceDataAccessor announceDataAccessor;

    public Announcer(final MockedTorrent torrent, final AnnounceDataAccessor announceDataAccessor) {
        this.torrent = torrent;
        this.trackerClient = new NewTrackerClient(torrent);
        this.announceDataAccessor = announceDataAccessor;
    }

    public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException, TooMuchAnnouncesFailedInARawException {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempt to announce {} for {}", event.getEventName(), this.torrent.getTorrentInfoHash().humanReadableValue());
        }

        try {
            final SuccessAnnounceResponse responseMessage = this.trackerClient.announce(
                    this.announceDataAccessor.getHttpRequestQueryForTorrent(this.torrent.getTorrentInfoHash(), event),
                    this.announceDataAccessor.getHttpHeadersForTorrent()
            );
            if (logger.isInfoEnabled()) {
                logger.info("{} has announced successfully. Response: {} seeders, {} leechers, {}s interval", this.torrent.getTorrentInfoHash().humanReadableValue(), responseMessage.getSeeders(), responseMessage.getLeechers(), responseMessage.getInterval());
            }

            this.consecutiveFails = 0;
            this.lastKnownInterval = responseMessage.getInterval();

            return responseMessage;
        } catch (final AnnounceException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} has failed to announce", this.torrent.getTorrentInfoHash().humanReadableValue(), e);
            }

            ++this.consecutiveFails;
            if (this.consecutiveFails == 5) {
                if (logger.isInfoEnabled()) {
                    logger.info("{} has failed to announce 5 times in a raw", this.torrent.getTorrentInfoHash().humanReadableValue());
                }
                throw new TooMuchAnnouncesFailedInARawException(torrent);
            }
            throw e;
        }
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

    public int getLastKnownInterval() {
        return lastKnownInterval;
    }

    public InfoHash getTorrentInfoHash() {
        return this.getTorrent().getTorrentInfoHash();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Announcer announcer = (Announcer) o;
        return Objects.equal(this.getTorrentInfoHash(), announcer.getTorrentInfoHash());
    }

    @Override
    public int hashCode() {
        return this.getTorrentInfoHash().hashCode();
    }
}
