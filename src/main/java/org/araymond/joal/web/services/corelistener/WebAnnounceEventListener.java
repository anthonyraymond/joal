package org.araymond.joal.web.services.corelistener;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WebAnnounceEventListener extends WebEventListener {
    @Inject
    public WebAnnounceEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void failedToAnnounce(final FailedToAnnounceEvent event) {
        log.debug("Send FailedToAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new FailedToAnnouncePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void successfullyAnnounce(final SuccessfullyAnnounceEvent event) {
        log.debug("Send SuccessfullyAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new SuccessfullyAnnouncePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void tooManyAnnouncesFailed(final TooManyAnnouncesFailedEvent event) {
        log.debug("Send TooManyAnnouncesFailedPayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new TooManyAnnouncesFailedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void willAnnounce(final WillAnnounceEvent event) {
        log.debug("Send WillAnnouncePayload to clients.");

        this.messagingTemplate.convertAndSend("/announce", new WillAnnouncePayload(event));
    }

}
