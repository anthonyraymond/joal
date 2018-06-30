package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import static org.slf4j.LoggerFactory.getLogger;

public class AnnounceEventPublisher implements AnnounceResponseHandlerChainElement {
    private static final Logger logger = getLogger(AnnounceEventPublisher.class);
    private final ApplicationEventPublisher eventPublisher;

    public AnnounceEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish WillAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new WillAnnounceEvent(announcer, event));
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.STARTED));
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.STARTED, throwable.getMessage()));
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.NONE));
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.NONE, throwable.getMessage()));
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.STOPPED));
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.STOPPED, throwable.getMessage()));
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish TooManyAnnouncesFailedEvent event for {}.", announcer.getTorrentInfoHash().humanReadableValue());
        }
        this.eventPublisher.publishEvent(new TooManyAnnouncesFailedEvent(announcer));
    }

}
