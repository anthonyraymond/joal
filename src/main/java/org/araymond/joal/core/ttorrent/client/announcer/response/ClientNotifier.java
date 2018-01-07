package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.ttorrent.client.Client;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientNotifier implements AnnounceResponseHandlerChainElement {
    private static final Logger logger = getLogger(ClientNotifier.class);
    private Client client;

    public void setClient(final Client client) {
        this.client = client;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (result.getSeeders() == 0 || result.getLeechers() == 0) {
            this.client.onNoMorePeers(announcer.getTorrent());
        }
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (result.getSeeders() < 1 || result.getLeechers() < 1) {
            this.client.onNoMorePeers(announcer.getTorrent());
        }
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Notify client that a torrent has stopped.");
        }
        this.client.onTorrentHasStopped(announcer);
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        if(logger.isDebugEnabled()) {
            logger.debug("Notify client that a torrent has stopped.");
        }
        this.client.onTooManyFailedInARaw(announcer);
    }
}
