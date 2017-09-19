package org.araymond.joal.web.config;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import javax.inject.Inject;

/**
 * Created by raymo on 22/06/2017.
 */
@ConditionalOnWebUi
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    private final String webSocketPathPrefix;

    @Inject
    public WebSocketConfig(@Value("${joal.ui.path.prefix}") final String webSocketPathPrefix) {
        this.webSocketPathPrefix = webSocketPathPrefix;
    }

    @Override
    public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(1000 * 1024) // Max outgoing message size => 1Mo
                .setSendBufferSizeLimit(5000 * 1024); // Max incoming message size => 5Mo
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker(
                "/global",
                "/announce",
                "/config",
                "/torrents"
        );
        config.setApplicationDestinationPrefixes("/joal");
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint(this.webSocketPathPrefix)
                .setAllowedOrigins("*");
    }

}
