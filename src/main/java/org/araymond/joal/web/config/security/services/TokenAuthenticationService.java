package org.araymond.joal.web.config.security.services;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by raymo on 30/07/2017.
 */
@ConditionalOnWebUi
@Service
public class TokenAuthenticationService {

    private final String secretToken;

    public TokenAuthenticationService(@Value("${joal.ui.secret-token}") final String secretToken) {
        this.secretToken = secretToken;
    }

    public Authentication getAuthentication(@Nullable final String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (this.secretToken.equals(token)) {
            final UserDetails userDetails = new JoalUserDetails("joal-user", token, Collections.singleton((GrantedAuthority) () -> "USER"));
            authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
        }
        return authentication;
    }

    private static class JoalUserDetails implements UserDetails {
        private static final long serialVersionUID = 5661362606649812883L;

        private final String username;
        private final String password;
        private final Collection<? extends GrantedAuthority> authorities;

        JoalUserDetails(final String username, final String password, final Collection<? extends GrantedAuthority> authorities) {
            this.username = username;
            this.password = password;
            this.authorities = new ArrayList<>(authorities);
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authorities;
        }

        @Override
        public String getPassword() {
            return this.password;
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
