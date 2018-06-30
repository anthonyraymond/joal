package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.speed.SeedingSpeedsHasChangedEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.speed.SeedingSpeedHasChangedPayload;
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
public class WebSpeedEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSpeedEventListener.class);

    @Inject
    public WebSpeedEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void failedToAnnounce(final SeedingSpeedsHasChangedEvent event) {
        logger.debug("Send SeedingSpeedHasChangedPayload to clients.");

        this.messagingTemplate.convertAndSend("/speed", new SeedingSpeedHasChangedPayload(event));
    }

}
