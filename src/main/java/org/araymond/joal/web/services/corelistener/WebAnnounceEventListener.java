package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.announce.FailedToAnnounceEvent;
import org.araymond.joal.core.events.announce.SuccessfullyAnnounceEvent;
import org.araymond.joal.core.events.announce.TooManyAnnouncesFailedEvent;
import org.araymond.joal.core.events.announce.WillAnnounceEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.announce.FailedToAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.SuccessfullyAnnouncePayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.TooManyAnnouncesFailedPayload;
import org.araymond.joal.web.messages.outgoing.impl.announce.WillAnnouncePayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void failedToAnnounce(final FailedToAnnounceEvent event) {
        logger.debug("Send FailedToAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new FailedToAnnouncePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void successfullyAnnounce(final SuccessfullyAnnounceEvent event) {
        logger.debug("Send SuccessfullyAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new SuccessfullyAnnouncePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void tooManyAnnouncesFailed(final TooManyAnnouncesFailedEvent event) {
        logger.debug("Send TooManyAnnouncesFailedPayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new TooManyAnnouncesFailedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void willAnnounce(final WillAnnounceEvent event) {
        logger.debug("Send WillAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new WillAnnouncePayload(event));
    }

}
