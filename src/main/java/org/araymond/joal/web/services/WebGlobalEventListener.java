package org.araymond.joal.web.services;

import org.araymond.joal.core.events.seedsession.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.seedsession.SeedSessionHasStartedEvent;
import org.araymond.joal.web.messages.outgoing.impl.SeedSessionHasEndedMessage;
import org.araymond.joal.web.messages.outgoing.impl.SeedSessionHasStartedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by raymo on 22/06/2017.
 */
@Service
public class WebGlobalEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebGlobalEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public WebGlobalEventListener(final SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final SeedSessionHasStartedEvent event) {
        logger.debug("Send SeedSessionHasStartedMessage to clients.");

        final String client = event.getBitTorrentClient().getHeaders().stream()
                .map(Map.Entry::getKey)
                .filter("User-Agent"::equalsIgnoreCase)
                .findFirst()
                .orElse("Unknown");

        this.messagingTemplate.convertAndSend("/global", new SeedSessionHasStartedMessage(client));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final SeedSessionHasEndedEvent event) {
        logger.debug("Send SeedSessionHasEndedMessage to clients.");

        this.messagingTemplate.convertAndSend("/global", new SeedSessionHasEndedMessage());
    }

}
