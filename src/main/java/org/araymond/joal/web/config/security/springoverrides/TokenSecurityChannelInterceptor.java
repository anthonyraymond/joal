package org.araymond.joal.web.config.security.springoverrides;

import org.araymond.joal.web.config.security.services.TokenAuthenticationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Stack;

/**
 * Created by raymo on 29/07/2017.
 */
@ConditionalOnProperty(name = "spring.main.web-environment", havingValue = "true")
@Component
public class TokenSecurityChannelInterceptor extends ChannelInterceptorAdapter implements ExecutorChannelInterceptor {
    private final SecurityContext EMPTY_CONTEXT = SecurityContextHolder
            .createEmptyContext();
    private static final ThreadLocal<Stack<SecurityContext>> ORIGINAL_CONTEXT = new ThreadLocal<>();

    private final String authenticationHeaderName;

    private final TokenAuthenticationService tokenAuthenticationService;

    private final Authentication anonymous = new AnonymousAuthenticationToken("key",
            "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    @Inject
    public TokenSecurityChannelInterceptor(final TokenAuthenticationService tokenAuthenticationService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.authenticationHeaderName = "X-Joal-Auth-Token";
    }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        setup(message);
        return message;
    }

    @Override
    public void afterSendCompletion(final Message<?> message, final MessageChannel channel, final boolean sent, final Exception ex) {
        cleanup();
    }

    public Message<?> beforeHandle(final Message<?> message, final MessageChannel channel, final MessageHandler handler) {
        setup(message);
        return message;
    }

    public void afterMessageHandled(final Message<?> message, final MessageChannel channel, final MessageHandler handler, final Exception ex) {
        cleanup();
    }

    private void setup(final Message<?> message) {
        final SecurityContext currentContext = SecurityContextHolder.getContext();
        Stack<SecurityContext> contextStack = ORIGINAL_CONTEXT.get();
        if (contextStack == null) {
            contextStack = new Stack<>();
            ORIGINAL_CONTEXT.set(contextStack);
        }

        contextStack.push(currentContext);

        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(this.getAuthentication(message));
        SecurityContextHolder.setContext(context);
    }

    private Authentication getAuthentication(final Message message) {
        Authentication authentication = this.anonymous;

        final NativeMessageHeaderAccessor nativeMessageHeaderAccessor = NativeMessageHeaderAccessor.getAccessor(message, NativeMessageHeaderAccessor.class);
        final String authToken = nativeMessageHeaderAccessor.getFirstNativeHeader(this.authenticationHeaderName);

        authentication = this.tokenAuthenticationService.getAuthentication(authToken);

        return authentication;
    }

    private void cleanup() {
        final Stack<SecurityContext> contextStack = ORIGINAL_CONTEXT.get();

        if (contextStack == null || contextStack.isEmpty()) {
            SecurityContextHolder.clearContext();
            ORIGINAL_CONTEXT.remove();
            return;
        }

        final SecurityContext originalContext = contextStack.pop();

        try {
            if (EMPTY_CONTEXT.equals(originalContext)) {
                SecurityContextHolder.clearContext();
                ORIGINAL_CONTEXT.remove();
            } else {
                SecurityContextHolder.setContext(originalContext);
            }
        } catch (final Throwable t) {
            SecurityContextHolder.clearContext();
        }
    }
}
