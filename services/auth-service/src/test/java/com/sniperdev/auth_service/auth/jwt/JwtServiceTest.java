package com.sniperdev.auth_service.auth.jwt;

import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "8fMOIwxy8XNDEzdeXYv7vJyKkm6VMONIdCjpFyapyuY=",
                3600000L
        );

        jwtService = new JwtService(properties);
    }

    @Test
    void shouldGenerateValidJwtToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .role(UserRole.STUDENT)
                .build();

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertThat(jwtService.isTokenValid("invalid.token")).isFalse();
    }

}