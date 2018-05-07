package org.araymond.joal.web.config.security;

import com.google.common.annotations.VisibleForTesting;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.config.security.websocket.interceptor.AuthChannelInterceptorAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.inject.Inject;

/**
 * Created by raymo on 30/07/2017.
 */
@ConditionalOnWebUi
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationSecurityConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthChannelInterceptorAdapter authChannelInterceptorAdapter;

    @Inject
    public WebSocketAuthenticationSecurityConfig(final AuthChannelInterceptorAdapter authChannelInterceptorAdapter) {
        this.authChannelInterceptorAdapter = authChannelInterceptorAdapter;
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        // Endpoints are already registered on WebSocketConfig, no need to add more.
    }

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.interceptors(this.createChannelInterceptors());
    }

    @VisibleForTesting
    ChannelInterceptor[] createChannelInterceptors() {
        return new ChannelInterceptor[]{this.authChannelInterceptorAdapter};
    }

}
