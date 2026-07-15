package com.sniperdev.auth_service.users.service;

import com.sniperdev.auth_service.common.exception.ConflictException;
import com.sniperdev.auth_service.common.exception.ResourceNotFoundException;
import com.sniperdev.auth_service.users.dto.CreateUserRequest;
import com.sniperdev.auth_service.users.dto.UserResponse;
import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserStatus;
import com.sniperdev.auth_service.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
