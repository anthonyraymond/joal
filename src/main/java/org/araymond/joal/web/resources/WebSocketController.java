package org.araymond.joal.web.resources;

import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.AppConfigurationIntegrityException;
import org.araymond.joal.web.messages.incoming.config.ConfigIncomingMessage;
import org.araymond.joal.web.messages.outgoing.StompMessage;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Created by raymo on 28/07/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final SeedManager seedManager;
    private final JoalMessageSendingTemplate messageSendingTemplate;

    @Inject
    public WebSocketController(final SeedManager seedManager, final JoalMessageSendingTemplate messageSendingTemplate) {
        this.seedManager = seedManager;
        this.messageSendingTemplate = messageSendingTemplate;
    }


    @MessageMapping("/config/save")
    public void saveNewConf(final ConfigIncomingMessage message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Client ask to save new conf {}", message.toString());
        }

        try {
            seedManager.saveNewConfiguration(message.toAppConfiguration());
        } catch (final AppConfigurationIntegrityException e) {
            logger.warn("Failed to save conf {}", message.toString(), e);
            messageSendingTemplate.convertAndSend("/config", new InvalidConfigPayload(e));
        }
    }

    @MessageMapping("/global/start")
    public void stopStartSession() throws IOException {
        seedManager.startSeeding();
    }

    @MessageMapping("/global/stop")
    public void stopSeedSession() {
        seedManager.stop();
    }

    /**
     * This mapping is bypassing the spring WebSocket broker (because of SubscribeMapping) and send the response
     * directly to the client who subscribed, and only him.
     * <b>READ CAREFULLY</b>: since the response is send ignoring the message broker, client will have to provide the
     * application destination prefix (/joal/events/replay)
     *
     * @return an ordered list of all needed event to rebuild the current application state
     */
    @SubscribeMapping("/events/replay")
    public List<StompMessage> list() {
        return messageSendingTemplate.getReplayablePayloads();
    }

}
