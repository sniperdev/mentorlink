package com.sniperdev.auth_service.users.service;

import com.sniperdev.auth_service.common.exception.ConflictException;
import com.sniperdev.auth_service.common.exception.InvalidCredentialsException;
import com.sniperdev.auth_service.users.model.User;
import com.sniperdev.auth_service.users.model.UserRole;
import com.sniperdev.auth_service.users.model.UserStatus;
import com.sniperdev.auth_service.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            UserRole role
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with this email already exists");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    }
}
