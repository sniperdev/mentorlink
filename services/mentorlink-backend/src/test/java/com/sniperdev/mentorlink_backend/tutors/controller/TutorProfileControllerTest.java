package com.sniperdev.mentorlink_backend.tutors.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sniperdev.mentorlink_backend.tutors.dto.CreateTutorProfileRequest;
import com.sniperdev.mentorlink_backend.tutors.dto.TutorProfileResponse;
import com.sniperdev.mentorlink_backend.tutors.service.TutorProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TutorProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class TutorProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private TutorProfileService tutorService;
    @Autowired
    private TutorProfileService tutorProfileService;

    @Test
    void shouldCreateTutorProfile() throws Exception {

        // given

        UUID userId = UUID.randomUUID();

        CreateTutorProfileRequest request =
                new CreateTutorProfileRequest(userId, "bio");

        TutorProfileResponse response =
                new TutorProfileResponse(
                        UUID.randomUUID(),
                        userId,
                        "bio",
                        true,
                        Instant.now(),
                        Instant.now()
                );

        when(tutorProfileService.createTutorProfile(any(CreateTutorProfileRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/tutors/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.bio").value("bio"))
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                );

        verify(tutorProfileService).createTutorProfile(any(CreateTutorProfileRequest.class));

    }
}