package com.sniperdev.mentorlink_backend.tutors.dto;

import java.util.UUID;

public record TutorSummaryResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String bio
) {
}
