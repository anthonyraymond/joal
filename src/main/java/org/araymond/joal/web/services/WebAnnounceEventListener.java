package org.araymond.joal.web.services;

import org.araymond.joal.core.events.announce.*;
import org.araymond.joal.web.messages.outgoing.impl.announce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * Created by raymo on 25/06/2017.
 */
@Service
public class WebAnnounceEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebAnnounceEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public WebAnnounceEventListener(final SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerHasStarted(final AnnouncerHasStartedEvent event) {
        logger.debug("Send AnnouncerHasStartedEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerHasStartedMessage(event.getTorrent()));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnounceHasStopped(final AnnouncerHasStoppedEvent event) {
        logger.debug("Send AnnouncerHasStoppedEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerHasStoppedMessage(event.getTorrent()));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerWillAnnounce(final AnnouncerWillAnnounceEvent event) {
        logger.debug("Send AnnouncerWillAnnounceEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerWillAnnounceMessage(event.getTorrent()));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerHasAnnounced(final AnnouncerHasAnnouncedEvent event) {
        logger.debug("Send AnnouncerHasAnnouncedEvent to clients.");

        this.messagingTemplate.convertAndSend(
                "/announce",
                new AnnouncerHasAnnouncedMessage(
                        event.getTorrent(),
                        event.getInterval(),
                        event.getSeeders(),
                        event.getLeechers()
                )
        );
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerFailedToAnnounce(final AnnouncerHasFailedToAnnounceEvent event) {
        logger.debug("Send AnnouncerHasFailedToAnnounceEvent to clients.");

        this.messagingTemplate.convertAndSend(
                "/announce",
                new AnnouncerHasFailedToAnnounceMessage(event.getTorrent(), event.getError())
        );
    }

}
