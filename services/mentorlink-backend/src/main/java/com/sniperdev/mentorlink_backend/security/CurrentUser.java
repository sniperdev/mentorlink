package com.sniperdev.mentorlink_backend.security;

import java.util.UUID;

public record CurrentUser(
        UUID id,
        UserRole role
) {
}
