package com.sniperdev.auth_service.users.dto;

import com.sniperdev.auth_service.users.model.UserRole;
import com.sniperdev.auth_service.users.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
