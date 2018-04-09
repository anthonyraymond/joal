package org.araymond.joal.web.config.security;

import org.araymond.joal.web.config.security.websocket.services.WebSocketAuthenticatorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class WebSocketAuthenticationSecurityConfigTest {

    @Test
    public void shouldRegisterInterceptors() {
        final WebSocketAuthenticatorService authService = mock(WebSocketAuthenticatorService.class);
        final WebSocketAuthenticationSecurityConfig webSocketAuthenticationSecurityConfig = spy(new WebSocketAuthenticationSecurityConfig(authService));

        final ChannelRegistration registration = mock(ChannelRegistration.class);
        webSocketAuthenticationSecurityConfig.configureClientInboundChannel(registration);

        verify(registration, times(1)).interceptors(Matchers.any());
        verify(webSocketAuthenticationSecurityConfig, times(1)).createChannelInterceptors();
    }
}
