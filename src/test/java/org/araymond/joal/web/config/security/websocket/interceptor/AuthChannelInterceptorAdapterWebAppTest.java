package org.araymond.joal.web.config.security.websocket.interceptor;

import org.araymond.joal.TestConstant;
import org.araymond.joal.web.config.WebSocketConfig;
import org.araymond.joal.web.config.security.websocket.services.WebSocketAuthenticatorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                AuthChannelInterceptorAdapter.class,
                WebSocketConfig.class,
                org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration.class,
                org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
                org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-environment=true",
                "joal.ui.secret-token=" + TestConstant.UI_SECRET_TOKEN
        }
)
@Import(AuthChannelInterceptorAdapterWebAppTest.TestWebSocketAuthenticationConfig.class)
public class AuthChannelInterceptorAdapterWebAppTest {

    @LocalServerPort
    private int port;

    @MockBean
    private WebSocketAuthenticatorService authenticatorService;

    @TestConfiguration
    public static class TestWebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {
        private final AuthChannelInterceptorAdapter authChannelInterceptorAdapter;
        @Inject
        public TestWebSocketAuthenticationConfig(final AuthChannelInterceptorAdapter authChannelInterceptorAdapter) {
            this.authChannelInterceptorAdapter = authChannelInterceptorAdapter;
        }
        @Override
        public void registerStompEndpoints(final StompEndpointRegistry registry) {
            // Endpoints are already registered on WebSocketConfig, no need to add more.
        }
        @Override
        public void configureClientInboundChannel(final ChannelRegistration registration) {
            registration.interceptors(this.authChannelInterceptorAdapter);
        }
    }

    @Test
    public void shouldCallAuthServiceWhenUserTriesToConnect() throws InterruptedException, ExecutionException, TimeoutException {
        final WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        final StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add(AuthChannelInterceptorAdapter.USERNAME_HEADER, "john");
        stompHeaders.add(AuthChannelInterceptorAdapter.TOKEN_HEADER, TestConstant.UI_SECRET_TOKEN);

        stompClient.connect("ws://localhost:" + port + "/", new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
        }).get(10, TimeUnit.SECONDS);

        verify(authenticatorService, times(1)).getAuthenticatedOrFail("john", TestConstant.UI_SECRET_TOKEN);
    }
}
