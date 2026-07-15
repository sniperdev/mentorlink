package com.sniperdev.mentorlink_backend.tutors.dto;

import java.util.UUID;

public record TutorSummaryResponse(
        UUID id,
        UUID userId,
        String bio
) {
}
