package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.ttorrent.client.DelayQueue;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;

import static org.slf4j.LoggerFactory.getLogger;


public class AnnounceReEnqueuer implements AnnounceResponseHandlerChainElement {
    private static final Logger logger = getLogger(AnnounceReEnqueuer.class);
    private final DelayQueue<AnnounceRequest> delayQueue;

    public AnnounceReEnqueuer(final DelayQueue<AnnounceRequest> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue torrent {} in regular queue.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue torrent {} in start queue once again (because it failed).", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 5, ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue torrent {} in regular queue.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue torrent {} in regular queue once again (because it failed).", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), announcer.getLastKnownInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enqueue torrent {} in stop queue once again (because it failed).", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createStop(announcer), 0, ChronoUnit.SECONDS);
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
    }
}
