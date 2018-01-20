package org.araymond.joal.web.services.corelistener;

import org.araymond.joal.core.events.global.state.GlobalSeedStartedEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStoppedEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStartedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStoppedPayload;
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
@ConditionalOnWebUi
@Service
public class WebGlobalEventListener extends WebEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebGlobalEventListener.class);

    @Inject
    public WebGlobalEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasStarted(final GlobalSeedStartedEvent event) {
        logger.debug("Send GlobalSeedStartedPayload to clients.");

        final String client = event.getBitTorrentClient().getHeaders().stream()
                .filter(entry -> "User-Agent".equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Unknown");

        this.messagingTemplate.convertAndSend("/global", new GlobalSeedStartedPayload(client));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    void handleSeedSessionHasEnded(final GlobalSeedStoppedEvent event) {
        logger.debug("Send GlobalSeedStoppedPayload to clients.");

        this.messagingTemplate.convertAndSend("/global", new GlobalSeedStoppedPayload());
    }

}
