package org.araymond.joal.core.ttorrent.client.announcer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.apache.http.client.HttpClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerClient;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerClientUriProvider;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Announcer implements AnnouncerFacade {
    private static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    private int lastKnownInterval = 5;
    private int consecutiveFails = 0;
    private Integer lastKnownLeechers = null;
    private Integer lastKnownSeeders = null;
    private LocalDateTime lastAnnouncedAt = null;
    private final MockedTorrent torrent;
    private TrackerClient trackerClient;
    private final AnnounceDataAccessor announceDataAccessor;

    Announcer(final MockedTorrent torrent, final AnnounceDataAccessor announceDataAccessor, final HttpClient httpClient) {
        this.torrent = torrent;
        this.trackerClient = this.buildTrackerClient(torrent, httpClient);
        this.announceDataAccessor = announceDataAccessor;
    }

    private TrackerClient buildTrackerClient(final MockedTorrent torrent, HttpClient httpClient) {
        final List<URI> trackerURIs = torrent.getAnnounceList().stream() // Use a list to keep it ordered
                .sequential()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return new TrackerClient(new TrackerClientUriProvider(trackerURIs), new TrackerResponseHandler(), httpClient);
    }

    @VisibleForTesting
    void setTrackerClient(final TrackerClient trackerClient) {
        this.trackerClient = trackerClient;
    }

    public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException, TooMuchAnnouncesFailedInARawException {
        if (logger.isDebugEnabled()) {
            logger.debug("Attempt to announce {} for {}", event.getEventName(), this.torrent.getTorrentInfoHash().humanReadableValue());
        }

        try {
            this.lastAnnouncedAt = LocalDateTime.now();
            final SuccessAnnounceResponse responseMessage = this.trackerClient.announce(
                    this.announceDataAccessor.getHttpRequestQueryForTorrent(this.torrent.getTorrentInfoHash(), event),
                    this.announceDataAccessor.getHttpHeadersForTorrent()
            );
            if (logger.isInfoEnabled()) {
                logger.info("{} has announced successfully. Response: {} seeders, {} leechers, {}s interval", this.torrent.getTorrentInfoHash().humanReadableValue(), responseMessage.getSeeders(), responseMessage.getLeechers(), responseMessage.getInterval());
            }

            this.lastKnownInterval = responseMessage.getInterval();
            this.lastKnownLeechers = responseMessage.getLeechers();
            this.lastKnownSeeders = responseMessage.getSeeders();
            this.consecutiveFails = 0;

            return responseMessage;
        } catch (final Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} has failed to announce", this.torrent.getTorrentInfoHash().humanReadableValue(), e);
            }

            ++this.consecutiveFails;
            if (this.consecutiveFails >= 5) {
                if (logger.isInfoEnabled()) {
                    logger.info("{} has failed to announce 5 times in a raw", this.torrent.getTorrentInfoHash().humanReadableValue());
                }
                throw new TooMuchAnnouncesFailedInARawException(torrent);
            }
            throw e;
        }
    }

    @Override
    public int getLastKnownInterval() {
        return lastKnownInterval;
    }

    @Override
    public int getConsecutiveFails() {
        return consecutiveFails;
    }

    @Override
    public Optional<Integer> getLastKnownLeechers() {
        return Optional.ofNullable(lastKnownLeechers);
    }

    @Override
    public Optional<Integer> getLastKnownSeeders() {
        return Optional.ofNullable(lastKnownSeeders);
    }

    @Override
    public Optional<LocalDateTime> getLastAnnouncedAt() {
        return Optional.ofNullable(lastAnnouncedAt);
    }

    public MockedTorrent getTorrent() {
        return torrent;
    }

    @Override
    public String getTorrentName() {
        return this.torrent.getName();
    }

    @Override
    public long getTorrentSize() {
        return this.torrent.getSize();
    }

    @Override
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
