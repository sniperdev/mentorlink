package com.sniperdev.auth_service.auth.controller;

import com.sniperdev.auth_service.auth.dto.LoginRequest;
import com.sniperdev.auth_service.auth.dto.RegisterRequest;
import com.sniperdev.auth_service.auth.jwt.JwtService;
import com.sniperdev.auth_service.auth.service.AuthService;
import com.sniperdev.auth_service.common.exception.ConflictException;
import com.sniperdev.auth_service.common.exception.GlobalExceptionHandler;
import com.sniperdev.auth_service.common.exception.InvalidCredentialsException;
import com.sniperdev.auth_service.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnCreatedWhenRegisterSucceeds() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe"
        );

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                ;

        verify(authService).register(any(RegisterRequest.class), any());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "john@example.com",
                "password123",
                "John",
                "Doe"
        );

        doThrow(new ConflictException("User already exists"))
                .when(authService).register(any(RegisterRequest.class), any());

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "",
                "",
                ""
        );

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class), any());
    }

    @Test
    void shouldReturnOkWhenLoginSucceeds() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@example.com",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).login(any(LoginRequest.class), any());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@example.com",
                "password123"
        );

        doThrow(new InvalidCredentialsException())
                .when(authService).login(any(LoginRequest.class), any());

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginRequestIsInvalid() throws Exception {

        LoginRequest request = new LoginRequest(
                "invalid-email",
                ""
        );

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any());
    }
}