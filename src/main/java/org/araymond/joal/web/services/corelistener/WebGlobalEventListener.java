package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.global.SeedSessionHasEndedEvent;
import org.araymond.joal.core.events.global.SeedSessionHasStartedEvent;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasEndedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.SeedSessionHasStartedPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by raymo on 22/06/2017.
 */
@Service
public class WebGlobalEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebGlobalEventListener.class);

    @Inject
    public WebGlobalEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final SeedSessionHasStartedEvent event) {
        logger.debug("Send SeedSessionHasStartedPayload to clients.");

        final String client = event.getBitTorrentClient().getHeaders().stream()
                .filter(entry -> "User-Agent".equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Unknown");

        this.messagingTemplate.convertAndSend("/global", new SeedSessionHasStartedPayload(client));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final SeedSessionHasEndedEvent event) {
        logger.debug("Send SeedSessionHasEndedPayload to clients.");

        this.messagingTemplate.convertAndSend("/global", new SeedSessionHasEndedPayload());
    }

}
