package org.araymond.joal.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        // TODO : see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-stomp-authentication-token-based
        // TODO: about @Order(Ordered.HIGHEST_PRECEDENCE + 99)
        registration.setInterceptors(new ChannelInterceptorAdapter() {
            @Override
            public Message<?> preSend(final Message<?> message, final MessageChannel channel) {

                final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT == accessor.getCommand()) {
                    //Principal user = ... ; // access authentication header(s)
                    //accessor.setUser(user);
                }
                return message;
            }
        });
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint(this.webSocketPathPrefix)
                .setAllowedOrigins("*");
    }

}
