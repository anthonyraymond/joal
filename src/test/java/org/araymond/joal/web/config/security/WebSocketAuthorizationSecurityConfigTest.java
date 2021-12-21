package org.araymond.joal.web.config.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class WebSocketAuthorizationSecurityConfigTest {

    @Test
    public void shouldDisableCSRFProtection() {
        final WebSocketAuthorizationSecurityConfig config = new WebSocketAuthorizationSecurityConfig();

        assertThat(config.sameOriginDisabled()).isTrue();
    }

}
