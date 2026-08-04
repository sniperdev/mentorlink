package com.sniperdev.auth_service.auth.refresh.service;

import com.sniperdev.auth_service.auth.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtProperties jwtProperties;

    public void addAccessTokenCookie(
            HttpServletResponse response,
            String accessToken
    ) {

        addCookie(
                response,
                "access_token",
                accessToken,
                Duration.ofMillis(jwtProperties.accessTokenExpiration()),
                "/"
        );
    }

    public void addRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken
    ) {

        addCookie(
                response,
                "refresh_token",
                refreshToken,
                Duration.ofMillis(jwtProperties.refreshTokenExpiration()),
                "/"
        );
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            Duration maxAge,
            String path
    ) {
        ResponseCookie cookie = ResponseCookie
                .from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}
