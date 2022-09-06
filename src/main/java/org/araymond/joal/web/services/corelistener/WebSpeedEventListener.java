package org.araymond.joal.web.services.corelistener;

import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.speed.SeedingSpeedHasChangedPayload;
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
public class WebSpeedEventListener extends WebEventListener {
    @Inject
    public WebSpeedEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void failedToAnnounce(final SeedingSpeedsHasChangedEvent event) {
        log.debug("Send SeedingSpeedHasChangedPayload to clients.");

        this.messagingTemplate.convertAndSend("/speed", new SeedingSpeedHasChangedPayload(event));
    }

}
