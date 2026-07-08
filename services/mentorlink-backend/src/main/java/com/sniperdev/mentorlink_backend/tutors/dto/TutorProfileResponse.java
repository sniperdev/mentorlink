package com.sniperdev.mentorlink_backend.tutors.dto;

import java.time.Instant;
import java.util.UUID;

public record TutorProfileResponse(
        UUID id,
        UUID userId,
        String bio,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
