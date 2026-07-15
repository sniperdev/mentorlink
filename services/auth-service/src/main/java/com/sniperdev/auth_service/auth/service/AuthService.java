package com.sniperdev.auth_service.auth.service;

import com.sniperdev.auth_service.auth.dto.AuthenticationResponse;
import com.sniperdev.auth_service.auth.dto.LoginRequest;
import com.sniperdev.auth_service.auth.dto.RegisterRequest;
import com.sniperdev.auth_service.auth.jwt.JwtService;
import com.sniperdev.auth_service.common.exception.InvalidCredentialsException;
import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import com.sniperdev.auth_service.users.service.UserService;
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

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());

        User user = userService.createUser(
                request.email(),
                passwordHash,
                request.firstName(),
                request.lastName(),
                UserRole.STUDENT
        );

        String token = jwtService.generateAccessToken(user);

        return new AuthenticationResponse(token);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {

        User user = userService.getByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthenticationResponse(accessToken);
    }
}
