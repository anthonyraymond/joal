package org.araymond.joal.core.ttorrent.client.announcer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.turn.ttorrent.client.announce.AnnounceException;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.torrent.torrent.MockedTorrent;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceDataAccessor;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerClient;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerClientUriProvider;
import org.araymond.joal.core.ttorrent.client.announcer.tracker.TrackerResponseHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

@Slf4j
public class Announcer implements AnnouncerFacade {
    @Getter private int lastKnownInterval = 5;
    @Getter private int consecutiveFails;
    private Integer lastKnownLeechers = null;
    private Integer lastKnownSeeders = null;
    private LocalDateTime lastAnnouncedAt = null;
    @Getter private final MockedTorrent torrent;
    private TrackerClient trackerClient;
    private final AnnounceDataAccessor announceDataAccessor;
    private long reportedUploadBytes = 0L;
    private final float uploadRatioTarget;

    Announcer(final MockedTorrent torrent, final AnnounceDataAccessor announceDataAccessor, final HttpClient httpClient, final float uploadRatioTarget) {
        this.torrent = torrent;
        this.trackerClient = this.buildTrackerClient(torrent, httpClient);
        this.announceDataAccessor = announceDataAccessor;
        this.uploadRatioTarget = uploadRatioTarget;
    }

    private TrackerClient buildTrackerClient(final MockedTorrent torrent, HttpClient httpClient) {
        final List<URI> trackerURIs = torrent.getAnnounceList().stream()  // Use a list to keep it ordered
                .sequential()
                .flatMap(Collection::stream)
                .collect(toList());
        return new TrackerClient(new TrackerClientUriProvider(trackerURIs), new TrackerResponseHandler(), httpClient);
    }

    @VisibleForTesting
    void setTrackerClient(final TrackerClient trackerClient) {
        this.trackerClient = trackerClient;
    }

    public SuccessAnnounceResponse announce(final RequestEvent event) throws AnnounceException, TooManyAnnouncesFailedInARowException {
        log.debug("Attempt to announce {} for {}", event.getEventName(), this.torrent.getTorrentInfoHash().getHumanReadable());

        try {
            this.lastAnnouncedAt = LocalDateTime.now();
            final SuccessAnnounceResponse responseMessage = this.trackerClient.announce(
                    this.announceDataAccessor.getHttpRequestQueryForTorrent(this.torrent.getTorrentInfoHash(), event),
                    this.announceDataAccessor.getHttpHeadersForTorrent()
            );
            log.info("{} has announced successfully. Response: {} seeders, {} leechers, {}s interval. Uploaded: {}, Speed: {}/s",
                    this.torrent.getTorrentInfoHash().getHumanReadable(), responseMessage.getSeeders(), responseMessage.getLeechers(), responseMessage.getInterval(),
                    byteCountToDisplaySize(announceDataAccessor.getUploaded(this.torrent.getTorrentInfoHash())),
                    byteCountToDisplaySize(announceDataAccessor.getSpeedInBytesPerSecond(this.torrent.getTorrentInfoHash())));

            this.reportedUploadBytes = announceDataAccessor.getUploaded(this.torrent.getTorrentInfoHash());
            this.lastKnownInterval = responseMessage.getInterval();
            this.lastKnownLeechers = responseMessage.getLeechers();
            this.lastKnownSeeders = responseMessage.getSeeders();
            this.consecutiveFails = 0;  // reset failure tally

            return responseMessage;
        } catch (final Exception e) {
            this.consecutiveFails++;
            if (this.consecutiveFails >= 5) {  // TODO: move to config
                log.warn("[{}] has failed to announce {} times in a row", this.torrent.getTorrentInfoHash().getHumanReadable(), this.consecutiveFails);
                throw new TooManyAnnouncesFailedInARowException(torrent);
            } else {
                log.info("[{}] has failed to announce {}. time", this.torrent.getTorrentInfoHash().getHumanReadable(), this.consecutiveFails);
            }

            throw e;
        }
    }

    @Override
    public Optional<Integer> getLastKnownLeechers() {
        return ofNullable(lastKnownLeechers);
    }

    @Override
    public Optional<Integer> getLastKnownSeeders() {
        return ofNullable(lastKnownSeeders);
    }

    @Override
    public Optional<LocalDateTime> getLastAnnouncedAt() {
        return ofNullable(lastAnnouncedAt);
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

    public boolean hasReachedUploadRatioLimit() {
        if (uploadRatioTarget == -1f) {
            return false;
        }
        final float bytesToUploadTarget = (uploadRatioTarget * (float) this.getTorrentSize());
        return reportedUploadBytes >= bytesToUploadTarget;
    }

    /**
     * Make sure to keep {@code torrentInfoHash} as the only input.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equal(this.getTorrentInfoHash(), ((Announcer) o).getTorrentInfoHash());
    }

    /**
     * Make sure to keep {@code torrentInfoHash} as the only input.
     */
    @Override
    public int hashCode() {
        return this.getTorrentInfoHash().hashCode();
    }
}
