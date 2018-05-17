package org.araymond.joal.web.config.security.websocket.services;

import org.araymond.joal.TestConstant;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WebSocketAuthenticatorServiceTest {

    @Test
    public void shouldThrowExceptionOnNullOrEmptyUsername() {
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);
        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("         ", TestConstant.UI_SECRET_TOKEN))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Username");

        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("", TestConstant.UI_SECRET_TOKEN))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Username");

        assertThatThrownBy(() -> authService.getAuthenticatedOrFail(null, TestConstant.UI_SECRET_TOKEN))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Username");
    }

    @Test
    public void shouldThrowExceptionOnNullOrEmptyToken() {
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);
        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("john", "         "))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Authentication token");

        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("john", ""))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Authentication token");

        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("john", null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessageContaining("Authentication token");
    }

    @Test
    public void shouldThrowExceptionIfTokenDoesNotMatches() {
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);
        assertThatThrownBy(() -> authService.getAuthenticatedOrFail("john", "nop"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Authentication token does not match");
    }

    @Test
    public void shouldReturnAuthenticationTokenOnSuccess() {
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);

        final UsernamePasswordAuthenticationToken authToken = authService.getAuthenticatedOrFail("john", TestConstant.UI_SECRET_TOKEN);

        assertThat(authToken.getName()).isEqualTo("john");
    }

    @Test
    public void shouldReturnInstanceOfUsernamePasswordAuthenticationTokenOnSuccess() {
        // This is not a useless test, Spring security chain test if the instance of the returned AuthToken is UsernamePasswordAuthenticationToken
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);

        final UsernamePasswordAuthenticationToken authToken = authService.getAuthenticatedOrFail("john", TestConstant.UI_SECRET_TOKEN);

        assertThat(authToken).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    @Test
    public void shouldDefineAtLeastOneGrantedAuthorityOnSuccess() {
        // This is not a useless test, Spring security chain test if there is at least one granted authority, if there is none, we are considered as non authenticated
        final WebSocketAuthenticatorService authService = new WebSocketAuthenticatorService(TestConstant.UI_SECRET_TOKEN);

        final UsernamePasswordAuthenticationToken authToken = authService.getAuthenticatedOrFail("john", TestConstant.UI_SECRET_TOKEN);

        assertThat(authToken.getAuthorities().size()).isGreaterThanOrEqualTo(1);
    }

}
