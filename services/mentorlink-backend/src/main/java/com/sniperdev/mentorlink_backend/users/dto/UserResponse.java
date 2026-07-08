package com.sniperdev.mentorlink_backend.users.dto;

import com.sniperdev.mentorlink_backend.users.model.UserRole;
import com.sniperdev.mentorlink_backend.users.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
