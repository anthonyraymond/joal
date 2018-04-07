package org.araymond.joal.web.config;

import org.araymond.joal.TestConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.inject.Inject;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                WebSocketConfig.class,
                org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.class,
                org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration.class,
                org.springframework.boot.autoconfigure.websocket.WebSocketMessagingAutoConfiguration.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-environment=true",
                "joal.ui.path.prefix=" + TestConstant.UI_PATH_PREFIX
        }
)
@Import({WebSocketConfigWebAppTest.TestStompMappings.class, WebSocketConfigWebAppTest.Dd.class})
public class WebSocketConfigWebAppTest {
    @LocalServerPort
    private int port;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Inject
    private MessagingCallback messagingCallback;

    @Before
    public void setUp() {
        reset(this.messagingCallback);
    }

    static class MessagingCallback {
        void global() {}
        void announce() {}
        void config() {}
        void torrents() {}
        void speed() {}
    }

    @TestConfiguration
    public static class Dd {
        @Bean
        public MessagingCallback callback() { return Mockito.spy(new MessagingCallback()); }
    }

    @Controller
    public static class TestStompMappings {
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Inject
        private MessagingCallback messagingCallback;
        @MessageMapping("/global")
        public void global() {
            messagingCallback.global();
        }
        @MessageMapping("/announce")
        public void announce() {
            messagingCallback.announce();
        }
        @MessageMapping("/config")
        public void config() {
            messagingCallback.config();
        }
        @MessageMapping("/torrents")
        public void torrents() {
            messagingCallback.torrents();
        }
        @MessageMapping("/speed")
        public void speed() {
            messagingCallback.speed();
        }
    }

    @Test
    public void shouldBeAbleToConnectToAppPrefix() throws InterruptedException, ExecutionException, TimeoutException {
        final WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        final StompSession stompSession = stompClient.connect("ws://localhost:" + port + "/" + TestConstant.UI_PATH_PREFIX, new StompSessionHandlerAdapter() {
        }).get(1000, TimeUnit.SECONDS);

        assertThat(stompSession.isConnected()).isTrue();
    }

    @Test
    public void shouldNotBeAbleToConnectWithoutAppPrefix() throws InterruptedException, ExecutionException, TimeoutException {
        final WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        assertThatThrownBy(() ->
                stompClient.connect("ws://localhost:" + port + "/", new StompSessionHandlerAdapter() {
                }).get(1000, TimeUnit.SECONDS)
        )
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("did not permit");
    }

    @Test
    public void shouldMapDestinationToMessageMappingWithDestinationPrefix() throws InterruptedException, ExecutionException, TimeoutException {
        final WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        final StompSession stompSession = stompClient.connect("ws://localhost:" + port + "/" + TestConstant.UI_PATH_PREFIX, new StompSessionHandlerAdapter() {
        }).get(10, TimeUnit.SECONDS);

        stompSession.send("/joal/global", null);
        verify(messagingCallback, timeout(1500).times(1)).global();

        stompSession.send("/joal/announce", null);
        verify(messagingCallback, timeout(1500).times(1)).announce();

        stompSession.send("/joal/config", null);
        verify(messagingCallback, timeout(1500).times(1)).config();

        stompSession.send("/joal/torrents", null);
        verify(messagingCallback, timeout(1500).times(1)).torrents();

        stompSession.send("/joal/speed", null);
        verify(messagingCallback, timeout(1500).times(1)).speed();
    }

    @Test
    public void shouldNotMapDestinationToMessageMappingWithoutDestinationPrefix() throws InterruptedException, ExecutionException, TimeoutException {
        final WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        final StompSession stompSession = stompClient.connect("ws://localhost:" + port + "/" + TestConstant.UI_PATH_PREFIX, new StompSessionHandlerAdapter() {
        }).get(10, TimeUnit.SECONDS);

        stompSession.send("/global", null);
        Thread.sleep(1500);
        verify(messagingCallback, timeout(1500).times(0)).global();
    }

}
