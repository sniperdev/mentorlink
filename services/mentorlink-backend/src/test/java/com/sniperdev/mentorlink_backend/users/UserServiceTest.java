package com.sniperdev.mentorlink_backend.users;

import com.sniperdev.mentorlink_backend.common.exception.ConflictException;
import com.sniperdev.mentorlink_backend.common.exception.ResourceNotFoundException;
import com.sniperdev.mentorlink_backend.users.dto.CreateUserRequest;
import com.sniperdev.mentorlink_backend.users.dto.UserResponse;
import com.sniperdev.mentorlink_backend.users.model.User;
import com.sniperdev.mentorlink_backend.users.model.UserRole;
import com.sniperdev.mentorlink_backend.users.model.UserStatus;
import com.sniperdev.mentorlink_backend.users.repository.UserRepository;
import com.sniperdev.mentorlink_backend.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // given
        CreateUserRequest request = new CreateUserRequest(
                "test@test.com",
                "password123",
                UserRole.STUDENT
        );

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .passwordHash(request.password())
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();


        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);


        // when
        UserResponse response = userService.createUser(request);


        // then
        assertThat(response.getEmail())
                .isEqualTo("test@test.com");

        assertThat(response.getRole())
                .isEqualTo(UserRole.STUDENT);


        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {

        // given

        CreateUserRequest request = new CreateUserRequest(
                "test@test.com",
                "password123",
                UserRole.STUDENT
        );


        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);


        // when + then

        assertThatThrownBy(
                () -> userService.createUser(request)
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already exists");


        verify(userRepository, never())
                .save(any());
    }


    @Test
    void shouldReturnUserById() {

        UUID id = UUID.randomUUID();


        User user = User.builder()
                .id(id)
                .email("test@test.com")
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();


        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));


        UserResponse response =
                userService.getUserById(id);


        assertThat(response.getId())
                .isEqualTo(id);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        UUID id = UUID.randomUUID();


        when(userRepository.findById(id))
                .thenReturn(Optional.empty());


        assertThatThrownBy(
                () -> userService.getUserById(id)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }
}