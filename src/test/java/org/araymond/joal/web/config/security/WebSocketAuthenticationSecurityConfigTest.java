package org.araymond.joal.web.config.security;

import org.araymond.joal.web.config.security.websocket.interceptor.AuthChannelInterceptorAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class WebSocketAuthenticationSecurityConfigTest {

    @Test
    public void shouldRegisterInterceptors() {
        final AuthChannelInterceptorAdapter authAdaptor = mock(AuthChannelInterceptorAdapter.class);
        final WebSocketAuthenticationSecurityConfig webSocketAuthenticationSecurityConfig = spy(new WebSocketAuthenticationSecurityConfig(authAdaptor));

        final ChannelRegistration registration = mock(ChannelRegistration.class);
        webSocketAuthenticationSecurityConfig.configureClientInboundChannel(registration);

        verify(registration, times(1)).interceptors(any());
        verify(webSocketAuthenticationSecurityConfig, times(1)).createChannelInterceptors();
    }

    @Test
    public void uselessTestButForCoverage() {
        final AuthChannelInterceptorAdapter authAdaptor = mock(AuthChannelInterceptorAdapter.class);
        final WebSocketAuthenticationSecurityConfig webSocketAuthenticationSecurityConfig = spy(new WebSocketAuthenticationSecurityConfig(authAdaptor));

        webSocketAuthenticationSecurityConfig.registerStompEndpoints(null);
    }
}
