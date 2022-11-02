package org.araymond.joal.core.ttorrent.client.announcer.response;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.core.ttorrent.client.announcer.Announcer;
import org.araymond.joal.core.ttorrent.client.announcer.exceptions.TooMuchAnnouncesFailedInARawException;
import org.araymond.joal.core.ttorrent.client.announcer.request.SuccessAnnounceResponse;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
@Slf4j
public class AnnounceEventPublisher implements AnnounceResponseHandlerChainElement {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onAnnouncerWillAnnounce(final Announcer announcer, final RequestEvent event) {
        log.debug("Publish WillAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new WillAnnounceEvent(announcer, event));
    }

    @Override
    public void onAnnounceStartSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.STARTED));
    }

    @Override
    public void onAnnounceStartFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.STARTED, throwable.getMessage()));
    }

    @Override
    public void onAnnounceRegularSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.NONE));
    }

    @Override
    public void onAnnounceRegularFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.NONE, throwable.getMessage()));
    }

    @Override
    public void onAnnounceStopSuccess(final Announcer announcer, final SuccessAnnounceResponse result) {
        log.debug("Publish SuccessfullyAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new SuccessfullyAnnounceEvent(announcer, RequestEvent.STOPPED));
    }

    @Override
    public void onAnnounceStopFails(final Announcer announcer, final Throwable throwable) {
        log.debug("Publish FailedToAnnounceEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new FailedToAnnounceEvent(announcer, RequestEvent.STOPPED, throwable.getMessage()));
    }

    @Override
    public void onTooManyAnnounceFailedInARaw(final Announcer announcer, final TooMuchAnnouncesFailedInARawException e) {
        log.debug("Publish TooManyAnnouncesFailedEvent event for {}.", announcer.getTorrentInfoHash().getHumanReadable());
        this.eventPublisher.publishEvent(new TooManyAnnouncesFailedEvent(announcer));
    }

}
