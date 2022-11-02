package org.araymond.joal.web.config.security.websocket.interceptor;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.araymond.joal.web.config.security.websocket.services.WebSocketAuthenticatorService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by raymo on 30/07/2017.
 */
@ConditionalOnWebUi
@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
    static final String USERNAME_HEADER = "X-Joal-Username";
    static final String TOKEN_HEADER = "X-Joal-Auth-Token";

    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Inject
    public AuthChannelInterceptorAdapter(final WebSocketAuthenticatorService webSocketAuthenticatorService) {
        this.webSocketAuthenticatorService = webSocketAuthenticatorService;
    }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            final String username = accessor.getFirstNativeHeader(USERNAME_HEADER);
            final String authToken = accessor.getFirstNativeHeader(TOKEN_HEADER);

            final Authentication user = webSocketAuthenticatorService.getAuthenticatedOrFail(username, authToken);

            accessor.setUser(user);
        }

        return message;
    }
}
