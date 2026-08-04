package com.sniperdev.auth_service.auth.security;

import com.sniperdev.auth_service.auth.jwt.JwtService;
import com.sniperdev.auth_service.users.service.UserService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterIntegrationTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userService);
    }

    @Test
    void shouldNotThrowOnInvalidToken() throws Exception {
        String invalidToken = "invalid.token.here";

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken);

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainCalled.set(true);

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, chain));
        assertTrue(chainCalled.get());
    }
}
