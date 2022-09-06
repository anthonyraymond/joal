package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.ttorrent.client.Client;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;

@Slf4j
public class ClientNotifier implements AnnounceResponseHandlerChainElement {
    private Client client;

    public void setClient(final Client client) {
        this.client = client;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (result.getSeeders() < 1 || result.getLeechers() < 1) {
            this.client.onNoMorePeers(announcer.getTorrentInfoHash());
        }
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (result.getSeeders() < 1 || result.getLeechers() < 1) {
            this.client.onNoMorePeers(announcer.getTorrentInfoHash());
        }
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Notify client that a torrent has stopped.");
        this.client.onTorrentHasStopped(announcer);
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        log.debug("Notify client that a torrent has failed too many times.");
        this.client.onTooManyFailedInARaw(announcer);
    }
}
