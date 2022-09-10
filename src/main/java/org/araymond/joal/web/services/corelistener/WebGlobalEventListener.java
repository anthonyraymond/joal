package org.araymond.joal.web.services.corelistener;

import lombok.extern.slf4j.Slf4j;
import org.araymond.joal.core.client.emulated.BitTorrentClientConfig;
import org.araymond.joal.core.events.global.state.GlobalSeedStartedEvent;
import org.araymond.joal.core.events.global.state.GlobalSeedStoppedEvent;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStartedPayload;
import org.araymond.joal.web.messages.outgoing.impl.global.state.GlobalSeedStoppedPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * Created by raymo on 22/06/2017.
 */
@ConditionalOnWebUi
@Service
@Slf4j
public class WebGlobalEventListener extends WebEventListener {
    @Inject
    public WebGlobalEventListener(final JoalMessageSendingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void globalSeedStarted(final GlobalSeedStartedEvent event) {
        log.debug("Send GlobalSeedStartedPayload to clients");

        final String client = event.getBitTorrentClient().getHeaders().stream()
                .filter(hdr -> USER_AGENT.equalsIgnoreCase(hdr.getName()))
                .findFirst()
                .map(BitTorrentClientConfig.HttpHeader::getValue)
                .orElse("Unknown");

        this.messagingTemplate.convertAndSend("/global", new GlobalSeedStartedPayload(client));
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener
    public void globalSeedStopped(@SuppressWarnings("unused") final GlobalSeedStoppedEvent event) {
        log.debug("Send GlobalSeedStoppedPayload to clients");

        this.messagingTemplate.convertAndSend("/global", new GlobalSeedStoppedPayload());
    }

}
