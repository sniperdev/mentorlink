package com.sniperdev.mentorlink_backend.subjects.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubjectRequest(
        @NotBlank
        String name
) {
}
