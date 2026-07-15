package com.sniperdev.mentorlink_backend.subjects.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sniperdev.mentorlink_backend.subjects.dto.CreateSubjectRequest;
import com.sniperdev.mentorlink_backend.subjects.dto.SubjectResponse;
import com.sniperdev.mentorlink_backend.subjects.service.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubjectController.class)
@WithMockUser
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SubjectService subjectService;

    @Test
    void shouldCreateSubject() throws Exception{

        CreateSubjectRequest request = new CreateSubjectRequest("Math");

        SubjectResponse response = new SubjectResponse(UUID.randomUUID(), "Math", "math", true, Instant.now(), Instant.now());

        when(subjectService.createSubject(any(CreateSubjectRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/subjects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Math"))
                .andExpect(jsonPath("$.slug").value("math"));

        verify(subjectService).createSubject(any(CreateSubjectRequest.class));
    }

    @Test
    void shouldReturnAllSubjects() throws Exception {

        SubjectResponse request = new SubjectResponse(UUID.randomUUID(), "Math", "math", true, Instant.now(), Instant.now());

        when(subjectService.getAllSubjects()).thenReturn(List.of(request));

        mockMvc.perform(
                get("/api/v1/subjects")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Math"))
                .andExpect(jsonPath("$[0].slug").value("math"));

    }

    @Test
    void shouldReturnSubjectById() throws Exception {
        UUID id = UUID.randomUUID();

        SubjectResponse response = new SubjectResponse(id, "Math", "math", true, Instant.now(), Instant.now());

        when(subjectService.getSubjectById(id)).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/subjects/{id}", id)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Math"))
                .andExpect(jsonPath("$.slug").value("math"));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {

        // Arrange
        CreateSubjectRequest request = new CreateSubjectRequest("");

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/subjects")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(subjectService);
    }
}