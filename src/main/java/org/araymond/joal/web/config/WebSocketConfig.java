package org.araymond.joal.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Created by raymo on 22/06/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    private final String webSocketPathPrefix;

    public WebSocketConfig(@Value("${joal.ui.path.prefix}")final String webSocketPathPrefix) {
        this.webSocketPathPrefix = webSocketPathPrefix;
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
