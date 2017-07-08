package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.config.ClientFilesDiscoveredEvent;
import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.web.messages.outgoing.impl.config.ClientFilesDiscoveredPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasBeenLoadedPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by raymo on 08/07/2017.
 */
@Service
public class WebConfigEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebConfigEventListener.class);

    @Inject
    public WebConfigEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleConfigHasBeenLoaded(final ConfigHasBeenLoadedEvent event) {
        logger.debug("Send ConfigHasBeenLoadedEvent to clients.");

        this.messagingTemplate.convertAndSend("/config", new ConfigHasBeenLoadedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleClientFilesDiscovered(final ClientFilesDiscoveredEvent event) {
        logger.debug("Send ClientFilesDiscoveredEvent to clients.");

        this.messagingTemplate.convertAndSend("/config", new ClientFilesDiscoveredPayload(event));
    }

}
