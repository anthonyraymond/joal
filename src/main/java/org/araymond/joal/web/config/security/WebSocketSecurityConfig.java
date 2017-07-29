package org.araymond.joal.web.config.security;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.config.security.springoverrides.JoalAbstractSecurityWebSocketMessageBrokerConfigurer;
import org.araymond.joal.web.config.security.springoverrides.TokenSecurityChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

/**
 * Created by raymo on 22/06/2017.
 */
@ConditionalOnWebUi
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig extends JoalAbstractSecurityWebSocketMessageBrokerConfigurer {

    public WebSocketSecurityConfig( final TokenSecurityChannelInterceptor tokenSecurityChannelInterceptor) {
        super(tokenSecurityChannelInterceptor);
    }

    @Override
    protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.MESSAGE,
                        SimpMessageType.SUBSCRIBE
                ).authenticated()
                .simpTypeMatchers(
                        SimpMessageType.UNSUBSCRIBE,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.CONNECT_ACK,
                        SimpMessageType.DISCONNECT_ACK,
                        SimpMessageType.OTHER // ACK
                ).permitAll()
                .anyMessage().denyAll();
    }

    // TODO : test purpose, investigate on this
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
