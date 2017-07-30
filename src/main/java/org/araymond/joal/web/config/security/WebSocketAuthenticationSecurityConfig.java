package org.araymond.joal.web.config.security;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.config.security.websocket.services.WebSocketAuthenticatorService;
import org.araymond.joal.web.config.security.websocket.interceptor.AuthChannelInterceptorAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.inject.Inject;

/**
 * Created by raymo on 30/07/2017.
 */
@ConditionalOnWebUi
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationSecurityConfig extends AbstractWebSocketMessageBrokerConfigurer {
    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Inject
    public WebSocketAuthenticationSecurityConfig(final WebSocketAuthenticatorService webSocketAuthenticatorService) {
        this.webSocketAuthenticatorService = webSocketAuthenticatorService;
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        // Endpoints are already registered on WebSocketConfig, no need to add more.
    }

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.setInterceptors(new AuthChannelInterceptorAdapter(this.webSocketAuthenticatorService));
    }

}
