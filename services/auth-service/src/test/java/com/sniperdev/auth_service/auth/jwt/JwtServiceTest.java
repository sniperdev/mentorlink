package com.sniperdev.auth_service.auth.jwt;

import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET_BASE64 = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes());
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
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
    void shouldGenerateValidToken() {
        when(jwtProperties.secret()).thenReturn(SECRET_BASE64);
        when(jwtProperties.expiration()).thenReturn(3600_000L);

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotNull();
        assertThat(token).contains(".");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldExtractUserId() {
        when(jwtProperties.secret()).thenReturn(SECRET_BASE64);
        when(jwtProperties.expiration()).thenReturn(3600_000L);

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnTrueForValidToken() {
        when(jwtProperties.secret()).thenReturn(SECRET_BASE64);
        when(jwtProperties.expiration()).thenReturn(3600_000L);

        String token = jwtService.generateAccessToken(user);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        when(jwtProperties.secret()).thenReturn(SECRET_BASE64);
        when(jwtProperties.expiration()).thenReturn(-1_000L);

        String token = jwtService.generateAccessToken(user);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isFalse();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        when(jwtProperties.secret()).thenReturn(SECRET_BASE64);
        when(jwtProperties.expiration()).thenReturn(3600_000L);

        String token = jwtService.generateAccessToken(user);

        String tampered = token.substring(0, token.length() - 1) + 'x';

        boolean valid = jwtService.isTokenValid(tampered);

        assertThat(valid).isFalse();
    }
}