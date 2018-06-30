package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.config.ConfigHasBeenLoadedEvent;
import org.araymond.joal.core.events.config.ConfigurationIsInDirtyStateEvent;
import org.araymond.joal.core.events.config.ListOfClientFilesEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigHasBeenLoadedPayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ConfigIsInDirtyStatePayload;
import org.araymond.joal.web.messages.outgoing.impl.config.ListOfClientFilesPayload;
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
@ConditionalOnWebUi
@Service
public class WebConfigEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebConfigEventListener.class);

    @Inject
    public WebConfigEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void configHasBeenLoaded(final ConfigHasBeenLoadedEvent event) {
        logger.debug("Send ConfigHasBeenLoadedPayload to clients.");

        this.messagingTemplate.convertAndSend("/config", new ConfigHasBeenLoadedPayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void configIsInDirtyState(final ConfigurationIsInDirtyStateEvent event) {
        logger.debug("Send ConfigIsInDirtyStatePayload to clients.");

        this.messagingTemplate.convertAndSend("/config", new ConfigIsInDirtyStatePayload(event));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void clientFilesDiscovered(final ListOfClientFilesEvent event) {
        logger.debug("Send ListOfClientFilesPayload to clients.");

        this.messagingTemplate.convertAndSend("/config", new ListOfClientFilesPayload(event));
    }

}
