package com.sniperdev.mentorlink_backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        Jwt jwt = getCurrentJwt();

        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                UserRole.valueOf(jwt.getClaimAsString("role"))
        );
    }

    private Jwt getCurrentJwt() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        return authentication.getToken();
    }
}
