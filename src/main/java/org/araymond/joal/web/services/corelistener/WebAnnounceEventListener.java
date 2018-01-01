package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by raymo on 25/06/2017.
 */
@ConditionalOnWebUi
@Service
public class WebAnnounceEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebAnnounceEventListener.class);

    @Inject
    public WebAnnounceEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }
/*
    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerHasStarted(final AnnouncerHasStartedEvent event) {
        logger.debug("Send AnnouncerHasStartedEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerHasStartedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnounceHasStopped(final AnnouncerHasStoppedEvent event) {
        logger.debug("Send AnnouncerHasStoppedEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerHasStoppedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerWillAnnounce(final AnnouncerWillAnnounceEvent event) {
        logger.debug("Send AnnouncerWillAnnounceEvent to clients.");

        this.messagingTemplate.convertAndSend("/announce", new AnnouncerWillAnnouncePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerHasAnnounced(final AnnouncerHasAnnouncedEvent event) {
        logger.debug("Send AnnouncerHasAnnouncedEvent to clients.");

        this.messagingTemplate.convertAndSend(
                "/announce",
                new AnnouncerHasAnnouncedPayload(event)
        );
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleAnnouncerFailedToAnnounce(final AnnouncerHasFailedToAnnounceEvent event) {
        logger.debug("Send AnnouncerHasFailedToAnnounceEvent to clients.");

        this.messagingTemplate.convertAndSend(
                "/announce",
                new AnnouncerHasFailedToAnnouncePayload(event)
        );
    }
*/
}
