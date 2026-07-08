package com.sniperdev.mentorlink_backend.users.service;

import com.sniperdev.mentorlink_backend.common.exception.ConflictException;
import com.sniperdev.mentorlink_backend.common.exception.ResourceNotFoundException;
import com.sniperdev.mentorlink_backend.users.dto.CreateUserRequest;
import com.sniperdev.mentorlink_backend.users.dto.UserResponse;
import com.sniperdev.mentorlink_backend.users.model.User;
import com.sniperdev.mentorlink_backend.users.model.UserStatus;
import com.sniperdev.mentorlink_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(request.password())
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToResponse).toList();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
