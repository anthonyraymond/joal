package org.araymond.joal.web.resources;

import org.araymond.joal.core.SeedManager;
import org.araymond.joal.core.config.AppConfigurationIntegrityException;
import org.araymond.joal.web.messages.incoming.config.ConfigIncomingMessage;
import org.araymond.joal.web.messages.outgoing.impl.config.InvalidConfigPayload;
import org.araymond.joal.web.services.JoalMessageSendingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;

/**
 * Created by raymo on 09/07/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Controller
@MessageMapping("/config")
public class ConfigController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private final SeedManager manager;
    private final JoalMessageSendingTemplate sendingTemplate;

    @Inject
    public ConfigController(final SeedManager manager, final JoalMessageSendingTemplate sendingTemplate) {
        this.manager = manager;
        this.sendingTemplate = sendingTemplate;
    }

    @MessageMapping("/save")
    public void saveNewConf(final ConfigIncomingMessage message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Client ask to save new conf {}", message.toString());
        }

        try {
            manager.saveNewConfiguration(message.toAppConfiguration());
        } catch (final AppConfigurationIntegrityException e) {
            logger.warn("Failed to save conf {}", message.toString(), e);
            sendingTemplate.convertAndSend("/config", new InvalidConfigPayload(e));
        }
    }

}
