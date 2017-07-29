package org.araymond.joal.web.config.security.springoverrides;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;
import org.springframework.security.messaging.access.expression.MessageExpressionVoter;
import org.springframework.security.messaging.access.intercept.ChannelSecurityInterceptor;
import org.springframework.security.messaging.access.intercept.MessageSecurityMetadataSource;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.web.csrf.CsrfChannelInterceptor;
import org.springframework.security.messaging.web.socket.server.CsrfTokenHandshakeInterceptor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;
import org.springframework.web.socket.sockjs.SockJsService;
import org.springframework.web.socket.sockjs.support.SockJsHttpRequestHandler;
import org.springframework.web.socket.sockjs.transport.TransportHandlingSockJsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by raymo on 29/07/2017.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@Import(ObjectPostProcessorConfiguration.class)
public class JoalAbstractSecurityWebSocketMessageBrokerConfigurer extends AbstractWebSocketMessageBrokerConfigurer implements SmartInitializingSingleton {
    private final WebSocketMessageSecurityMetadataSourceRegistry inboundRegistry = new WebSocketMessageSecurityMetadataSourceRegistry();

    private SecurityExpressionHandler<Message<Object>> defaultExpressionHandler = new DefaultMessageSecurityExpressionHandler<Object>();

    private SecurityExpressionHandler<Message<Object>> expressionHandler;

    private ApplicationContext context;

    private final TokenSecurityChannelInterceptor tokenSecurityChannelInterceptor;

    public JoalAbstractSecurityWebSocketMessageBrokerConfigurer(final TokenSecurityChannelInterceptor tokenSecurityChannelInterceptor) {
        this.tokenSecurityChannelInterceptor = tokenSecurityChannelInterceptor;
    }

    public void registerStompEndpoints(final StompEndpointRegistry registry) {
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public final void configureClientInboundChannel(final ChannelRegistration registration) {
        final ChannelSecurityInterceptor inboundChannelSecurity = inboundChannelSecurity();
        registration.setInterceptors(this.tokenSecurityChannelInterceptor);
        if (!sameOriginDisabled()) {
            registration.setInterceptors(csrfChannelInterceptor());
        }
        if (inboundRegistry.containsMapping()) {
            registration.setInterceptors(inboundChannelSecurity);
        }
        customizeClientInboundChannel(registration);
    }

    private PathMatcher getDefaultPathMatcher() {
        try {
            return context.getBean(SimpAnnotationMethodMessageHandler.class).getPathMatcher();
        } catch(final NoSuchBeanDefinitionException e) {
            return new AntPathMatcher();
        }
    }

    /**
     * <p>
     * Determines if a CSRF token is required for connecting. This protects against remote
     * sites from connecting to the application and being able to read/write data over the
     * connection. The default is false (the token is required).
     * </p>
     * <p>
     * Subclasses can override this method to disable CSRF protection
     * </p>
     *
     * @return false if a CSRF token is required for connecting, else true
     */
    protected boolean sameOriginDisabled() {
        return false;
    }

    /**
     * Allows subclasses to customize the configuration of the {@link ChannelRegistration}
     * .
     *
     * @param registration the {@link ChannelRegistration} to customize
     */
    protected void customizeClientInboundChannel(final ChannelRegistration registration) {
    }

    @Bean
    public CsrfChannelInterceptor csrfChannelInterceptor() {
        return new CsrfChannelInterceptor();
    }

    @Bean
    public ChannelSecurityInterceptor inboundChannelSecurity() {
        final ChannelSecurityInterceptor channelSecurityInterceptor = new ChannelSecurityInterceptor(
                inboundMessageSecurityMetadataSource());
        final MessageExpressionVoter<Object> voter = new MessageExpressionVoter<>();
        voter.setExpressionHandler(getMessageExpressionHandler());

        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(voter);

        final AccessDecisionManager manager = new AffirmativeBased(voters);
        channelSecurityInterceptor.setAccessDecisionManager(manager);
        return channelSecurityInterceptor;
    }

    @Bean
    public MessageSecurityMetadataSource inboundMessageSecurityMetadataSource() {
        inboundRegistry.expressionHandler(getMessageExpressionHandler());
        configureInbound(inboundRegistry);
        return inboundRegistry.createMetadataSource();
    }

    protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
    }

    private static class WebSocketMessageSecurityMetadataSourceRegistry extends
            MessageSecurityMetadataSourceRegistry {
        @Override
        public MessageSecurityMetadataSource createMetadataSource() {
            return super.createMetadataSource();
        }

        @Override
        protected boolean containsMapping() {
            return super.containsMapping();
        }

        @Override
        protected boolean isSimpDestPathMatcherConfigured() {
            return super.isSimpDestPathMatcherConfigured();
        }
    }

    @Autowired
    public void setApplicationContext(final ApplicationContext context) {
        this.context = context;
    }

    @Deprecated
    public void setMessageExpessionHandler(final List<SecurityExpressionHandler<Message<Object>>> expressionHandlers) {
        setMessageExpressionHandler(expressionHandlers);
    }

    @Autowired(required = false)
    public void setMessageExpressionHandler(final List<SecurityExpressionHandler<Message<Object>>> expressionHandlers) {
        if(expressionHandlers.size() == 1) {
            this.expressionHandler = expressionHandlers.get(0);
        }
    }

    @Autowired(required = false)
    public void setObjectPostProcessor(final ObjectPostProcessor<Object> objectPostProcessor) {
        defaultExpressionHandler = objectPostProcessor.postProcess(defaultExpressionHandler);
    }

    private  SecurityExpressionHandler<Message<Object>> getMessageExpressionHandler() {
        if(expressionHandler == null) {
            return defaultExpressionHandler;
        }
        return expressionHandler;
    }

    public void afterSingletonsInstantiated() {
        if (sameOriginDisabled()) {
            return;
        }

        final String beanName = "stompWebSocketHandlerMapping";
        final SimpleUrlHandlerMapping mapping = context.getBean(beanName,
                SimpleUrlHandlerMapping.class);
        final Map<String, Object> mappings = mapping.getHandlerMap();
        for (final Object object : mappings.values()) {
            if (object instanceof SockJsHttpRequestHandler) {
                final SockJsHttpRequestHandler sockjsHandler = (SockJsHttpRequestHandler) object;
                final SockJsService sockJsService = sockjsHandler.getSockJsService();
                if (!(sockJsService instanceof TransportHandlingSockJsService)) {
                    throw new IllegalStateException(
                            "sockJsService must be instance of TransportHandlingSockJsService got "
                                    + sockJsService);
                }

                final TransportHandlingSockJsService transportHandlingSockJsService = (TransportHandlingSockJsService) sockJsService;
                final List<HandshakeInterceptor> handshakeInterceptors = transportHandlingSockJsService
                        .getHandshakeInterceptors();
                final List<HandshakeInterceptor> interceptorsToSet = new ArrayList<>(
                        handshakeInterceptors.size() + 1);
                interceptorsToSet.add(new CsrfTokenHandshakeInterceptor());
                interceptorsToSet.addAll(handshakeInterceptors);

                transportHandlingSockJsService
                        .setHandshakeInterceptors(interceptorsToSet);
            }
            else if (object instanceof WebSocketHttpRequestHandler) {
                final WebSocketHttpRequestHandler handler = (WebSocketHttpRequestHandler) object;
                final List<HandshakeInterceptor> handshakeInterceptors = handler
                        .getHandshakeInterceptors();
                final List<HandshakeInterceptor> interceptorsToSet = new ArrayList<>(
                        handshakeInterceptors.size() + 1);
                interceptorsToSet.add(new CsrfTokenHandshakeInterceptor());
                interceptorsToSet.addAll(handshakeInterceptors);

                handler.setHandshakeInterceptors(interceptorsToSet);
            }
            else {
                throw new IllegalStateException(
                        "Bean "
                                + beanName
                                + " is expected to contain mappings to either a SockJsHttpRequestHandler or a WebSocketHttpRequestHandler but got "
                                + object);
            }
        }

        if (inboundRegistry.containsMapping() && !inboundRegistry.isSimpDestPathMatcherConfigured()) {
            final PathMatcher pathMatcher = getDefaultPathMatcher();
            inboundRegistry.simpDestPathMatcher(pathMatcher);
        }
    }
}
