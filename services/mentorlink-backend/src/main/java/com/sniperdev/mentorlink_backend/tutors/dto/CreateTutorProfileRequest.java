package com.sniperdev.mentorlink_backend.tutors.dto;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTutorProfileRequest (
        @NotNull
        UUID userId,

        @NotNull
        String bio
) {
}
