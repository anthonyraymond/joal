package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooManyAnnouncesFailedInARowException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

@RequiredArgsConstructor
@Slf4j
public class BandwidthDispatcherNotifier implements AnnounceResponseHandler {
    private final BandwidthDispatcher bandwidthDispatcher;

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
        // noop
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Register [{}] in bandwidth dispatcher and update stats", announcer.getTorrentInfoHash().getHumanReadable());
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.registerTorrent(infoHash);
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers(), announcer.getTorrentSize());
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        // noop
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Update [{}] stats in bandwidth dispatcher", announcer.getTorrentInfoHash().getHumanReadable());
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers(), announcer.getTorrentSize());
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        // noop
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Unregister [{}] from bandwidth dispatcher", announcer.getTorrentInfoHash().getHumanReadable());
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        // noop
    }

    @Override
    public void onTooManyAnnounceFailedInARow(final Announcer announcer, final TooManyAnnouncesFailedInARowException e) {
        log.debug("Unregister [{}] from bandwidth dispatcher", announcer.getTorrentInfoHash().getHumanReadable());
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }
}
