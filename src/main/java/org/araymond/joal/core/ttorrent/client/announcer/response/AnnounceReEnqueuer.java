package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.AnnounceRequest;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.araymond.joal.core.ttorrent.client.AvailableAfterIntervalQueue;

import java.time.temporal.ChronoUnit;


public class AnnounceReEnqueuer implements AnnounceResponseHandlerChainElement {
    private final AvailableAfterIntervalQueue<AnnounceRequest> delayQueue;

    public AnnounceReEnqueuer(final AvailableAfterIntervalQueue<AnnounceRequest> delayQueue) {
        this.delayQueue = delayQueue;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer) {
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        if (TooMuchAnnouncesFailedInARawException.class.isAssignableFrom(throwable.getClass())) {
            return;
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createStart(announcer), 5, ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), result.getInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        if (TooMuchAnnouncesFailedInARawException.class.isAssignableFrom(throwable.getClass())) {
            return;
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createRegular(announcer), announcer.getLastKnownInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        if (TooMuchAnnouncesFailedInARawException.class.isAssignableFrom(throwable.getClass())) {
            return;
        }
        this.delayQueue.addOrReplace(AnnounceRequest.createStop(announcer), announcer.getLastKnownInterval(), ChronoUnit.SECONDS);
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
    }
}
