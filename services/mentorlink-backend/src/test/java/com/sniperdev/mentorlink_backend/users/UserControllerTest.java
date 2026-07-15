package com.sniperdev.mentorlink_backend.users;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sniperdev.mentorlink_backend.users.controller.UserController;
import com.sniperdev.mentorlink_backend.users.dto.CreateUserRequest;
import com.sniperdev.mentorlink_backend.users.dto.UserResponse;
import com.sniperdev.mentorlink_backend.users.model.UserRole;
import com.sniperdev.mentorlink_backend.users.model.UserStatus;
import com.sniperdev.mentorlink_backend.users.service.UserService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {

        // given

        CreateUserRequest request = new CreateUserRequest(
                "test@test.com",
                "password123",
                "test",
                "test",
                UserRole.STUDENT
        );


        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "test@test.com",
                "test",
                "test",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );


        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(response);


        // when + then

        mockMvc.perform(
                        post("/api/v1/users")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("test@test.com"))
                .andExpect(jsonPath("$.role")
                        .value("STUDENT"));


        verify(userService)
                .createUser(any(CreateUserRequest.class));
    }


    @Test
    void shouldReturnAllUsers() throws Exception {

        UserResponse user = new UserResponse(
                UUID.randomUUID(),
                "test@test.com",
                "test",
                "test",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );


        when(userService.getAllUsers())
                .thenReturn(List.of(user));


        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email")
                        .value("test@test.com"));
    }


    @Test
    void shouldReturnUserById() throws Exception {

        UUID id = UUID.randomUUID();


        UserResponse response = new UserResponse(
                id,
                "test@test.com",
                "test",
                "test",
                UserRole.STUDENT,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );


        when(userService.getUserById(id))
                .thenReturn(response);


        mockMvc.perform(
                        get("/api/v1/users/{id}", id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.email")
                        .value("test@test.com"));
    }
}