package org.araymond.joal.core.ttorrent.client.announcer.response;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.torrent.torrent.InfoHash;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.Announcer;
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
    public void onAnnouncerWillAnnounce(final Announcer announcer) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish WillAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.eventPublisher.publishEvent(new WillAnnounceEvent(infoHash));
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        final int interval = result.getInterval();
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(infoHash, interval));
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        final int interval = announcer.getLastKnownInterval();
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(infoHash, interval));
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        final int interval = result.getInterval();
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(infoHash, interval));
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        final int interval = announcer.getLastKnownInterval();
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(infoHash, interval));
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(infoHash, 0));
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(infoHash, 0));
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        if(logger.isDebugEnabled()) {
            logger.debug("Publish TooManyAnnouncesFailedEvent event for {}.", announcer.getTorrentInfoHash().value());
        }
        final InfoHash infoHash = announcer.getTorrentInfoHash();
        this.eventPublisher.publishEvent(new TooManyAnnouncesFailedEvent(infoHash));
    }

}
