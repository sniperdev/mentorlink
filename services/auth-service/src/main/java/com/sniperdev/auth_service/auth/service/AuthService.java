package com.sniperdev.auth_service.auth.service;

import com.sniperdev.auth_service.auth.dto.LoginRequest;
import com.sniperdev.auth_service.auth.dto.RegisterRequest;
import com.sniperdev.auth_service.auth.jwt.JwtService;
import com.sniperdev.auth_service.auth.refresh.service.CookieService;
import com.sniperdev.auth_service.auth.refresh.service.RefreshTokenService;
import com.sniperdev.auth_service.common.exception.InvalidCredentialsException;
import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import com.sniperdev.auth_service.users.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Transactional
    public void register(RegisterRequest request, HttpServletResponse response) {
        String passwordHash = passwordEncoder.encode(request.password());

        User user = userService.createUser(
                request.email(),
                passwordHash,
                request.firstName(),
                request.lastName(),
                UserRole.STUDENT
        );

        authenticateUser(user, response);
    }

    @Transactional
    public void login(LoginRequest request, HttpServletResponse response) {

        User user = userService.getByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        authenticateUser(user, response);
    }

    private void authenticateUser(
            User user,
            HttpServletResponse response
    ) {
        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        cookieService.addAccessTokenCookie(response, accessToken);
        cookieService.addRefreshTokenCookie(response, refreshToken);
    }
}
