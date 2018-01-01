package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.bandwith.NewBandwidthDispatcher;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorent.client.announce.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

public class BandwidthDispatcherNotifier implements AnnounceResponseHandlerChainElement {
    private final NewBandwidthDispatcher bandwidthDispatcher;

    public BandwidthDispatcherNotifier(final NewBandwidthDispatcher bandwidthDispatcher) {
        this.bandwidthDispatcher = bandwidthDispatcher;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.registerTorrent(infoHash);
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers());
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.bandwidthDispatcher.updateTorrentPeers(infoHash, result.getSeeders(), result.getLeechers());
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        this.bandwidthDispatcher.unregisterTorrent(announcer.getTorrentInfoHash());
    }
}
