package org.araymond.joal.web.config.security.websocket.services;

import org.apache.commons.lang3.StringUtils;
import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by raymo on 30/07/2017.
 */
@ConditionalOnWebUi
@Component
public class WebSocketAuthenticatorService {
    private final String appSecretToken;

    public WebSocketAuthenticatorService(@Value("${joal.ui.secret-token}") final String appSecretToken) {
        this.appSecretToken = appSecretToken;
    }

    // This method must return a UsernamePasswordAuthenticationToken, another component in the security chain is testing it with 'instanceof'
    @SuppressWarnings("TypeMayBeWeakened")
    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final CharSequence username, final CharSequence authToken) throws AuthenticationException {
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
        }
        if (StringUtils.isBlank(authToken)) {
            throw new AuthenticationCredentialsNotFoundException("Authentication token was null or empty.");
        }
        if (!appSecretToken.contentEquals(authToken)) {
            throw new BadCredentialsException("Authentication token does not match the expected token");
        }

        // Everything is fine, return an authenticated Authentication. (the constructor with grantedAuthorities auto set authenticated = true)
        // null credentials, we do not pass the password along to prevent security flaw
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singleton((GrantedAuthority) () -> "USER")
        );
    }

}
