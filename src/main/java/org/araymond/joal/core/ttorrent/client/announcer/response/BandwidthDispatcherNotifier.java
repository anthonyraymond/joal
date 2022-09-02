package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.bandwith.BandwidthDispatcher;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class BandwidthDispatcherNotifier implements AnnounceResponseHandlerChainElement {
    private static final Logger logger = getLogger(BandwidthDispatcherNotifier.class);
    private final BandwidthDispatcher bandwidthDispatcher;

    public BandwidthDispatcherNotifier(final BandwidthDispatcher bandwidthDispatcher) {
        this.bandwidthDispatcher = bandwidthDispatcher;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        logger.debug("Register {} in bandwidth dispatcher and update stats.", announcer.getTorrentInfoHash().humanReadableValue());
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.registerTorrent(infoHash);
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers());
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        logger.debug("Update {} stats in bandwidth dispatcher.", announcer.getTorrentInfoHash().humanReadableValue());
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers());
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        logger.debug("Unregister {} from bandwidth dispatcher.", announcer.getTorrentInfoHash().humanReadableValue());
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        logger.debug("Unregister {} from bandwidth dispatcher.", announcer.getTorrentInfoHash().humanReadableValue());
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }
}
