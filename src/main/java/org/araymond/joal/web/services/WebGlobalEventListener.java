package org.araymond.joal.web.services;

import org.araymond.joal.core.client.emulated.BitTorrentClient;
import org.araymond.joal.core.events.seedsession.SeedSessionHasStartedEvent;
import org.araymond.joal.web.messages.outgoing.impl.ClientHasStartedMessage;
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
        logger.debug("Send ClientHasStartedMessage to client.");

        final String client = event.getBitTorrentClient().getHeaders().stream()
                .map(Map.Entry::getKey)
                .filter("User-Agent"::equalsIgnoreCase)
                .findFirst()
                .orElse("Unknown");

        messagingTemplate.convertAndSend("/global", new ClientHasStartedMessage(client));
    }

}
