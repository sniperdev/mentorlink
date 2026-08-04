package com.sniperdev.mentorlink_backend.tutors.dto;


import jakarta.validation.constraints.NotNull;

public record CreateTutorProfileRequest(
        @NotNull
        String bio
) {
}
