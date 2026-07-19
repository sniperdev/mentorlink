package com.sniperdev.auth_service.auth.service;

import com.sniperdev.auth_service.auth.dto.AuthenticationResponse;
import com.sniperdev.auth_service.auth.dto.LoginRequest;
import com.sniperdev.auth_service.auth.dto.RegisterRequest;
import com.sniperdev.auth_service.auth.jwt.JwtService;
import com.sniperdev.auth_service.common.exception.ConflictException;
import com.sniperdev.auth_service.common.exception.InvalidCredentialsException;
import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import com.sniperdev.auth_service.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {


    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe"
        );

        loginRequest = new LoginRequest(
                "john@example.com",
                "password123"
        );

        user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    void shouldRegisterUserAndReturnToken() {
        when(passwordEncoder.encode(registerRequest.password()))
                .thenReturn("hashedPassword");

        when(userService.createUser(
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(user);

        when(jwtService.generateAccessToken(user))
                .thenReturn("jwt-token");

        AuthenticationResponse response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("jwt-token");

        verify(passwordEncoder).encode(registerRequest.password());

        verify(userService).createUser(
                eq(registerRequest.email()),
                eq("hashedPassword"),
                eq(registerRequest.firstName()),
                eq(registerRequest.lastName()),
                eq(UserRole.STUDENT)
        );

        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        when(userService.createUser(
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenThrow(new ConflictException("User already exists"));

        assertThatThrownBy(() ->
                authService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("User already exists");

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void shouldLoginAndReturnToken() {

        when(userService.getByEmail(loginRequest.email()))
                .thenReturn(user);

        when(passwordEncoder.matches(
                loginRequest.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("jwt-token");

        AuthenticationResponse response =
                authService.login(loginRequest);

        assertThat(response.token())
                .isEqualTo("jwt-token");

        verify(passwordEncoder)
                .matches(loginRequest.password(), user.getPasswordHash());

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenPasswordIsInvalid() {

        when(userService.getByEmail(loginRequest.email()))
                .thenReturn(user);

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never())
                .generateAccessToken(any());
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenUserDoesNotExist() {

        when(userService.getByEmail(loginRequest.email()))
                .thenThrow(new InvalidCredentialsException());

        assertThatThrownBy(() ->
                authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never())
                .matches(any(), any());

        verify(jwtService, never())
                .generateAccessToken(any());
    }
}