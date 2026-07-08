package com.sniperdev.mentorlink_backend.tutors.service;

import com.sniperdev.mentorlink_backend.common.exception.ConflictException;
import com.sniperdev.mentorlink_backend.common.exception.ResourceNotFoundException;
import com.sniperdev.mentorlink_backend.tutors.dto.CreateTutorProfileRequest;
import com.sniperdev.mentorlink_backend.tutors.dto.TutorProfileResponse;
import com.sniperdev.mentorlink_backend.tutors.mapper.TutorProfileMapper;
import com.sniperdev.mentorlink_backend.tutors.model.TutorProfile;
import com.sniperdev.mentorlink_backend.tutors.repository.TutorProfileRepository;
import com.sniperdev.mentorlink_backend.users.model.User;
import com.sniperdev.mentorlink_backend.users.model.UserRole;
import com.sniperdev.mentorlink_backend.users.model.UserStatus;
import com.sniperdev.mentorlink_backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorProfileServiceTest {

    @Mock
    private TutorProfileRepository tutorProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TutorProfileMapper mapper;

    @InjectMocks
    private TutorProfileService tutorService;

    @Test
    void shouldCreateTutorProfile() {
        // given
        UUID userId = UUID.randomUUID();

        CreateTutorProfileRequest request =
                new CreateTutorProfileRequest(userId, "bio");

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .role(UserRole.TUTOR)
                .status(UserStatus.ACTIVE)
                .build();

        TutorProfile savedProfile =
                TutorProfile.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .bio(request.bio())
                        .build();

        TutorProfileResponse response =
                new TutorProfileResponse(
                        savedProfile.getId(),
                        userId,
                        savedProfile.getBio(),
                        true,
                        Instant.now(),
                        Instant.now()
                );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(tutorProfileRepository.existsByUserId(userId)).thenReturn(false);

        when(tutorProfileRepository.save(any(TutorProfile.class))).thenReturn(savedProfile);

        when(mapper.toResponse(savedProfile)).thenReturn(response);

        // when
        TutorProfileResponse result = tutorService.createTutorProfile(request);

        // then

        assertThat(result.userId()).isEqualTo(userId);

        assertThat(result.bio()).isEqualTo("bio");

        assertThat(result.active()).isTrue();

        verify(tutorProfileRepository).save(any(TutorProfile.class));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        CreateTutorProfileRequest request =
                new CreateTutorProfileRequest(userId, "bio");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tutorService.createTutorProfile(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(tutorProfileRepository, never()).save(any());

    }

    @Test
    void shouldThrowExceptionWhenUserIsNotTutor() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .role(UserRole.STUDENT)
                .build();

        CreateTutorProfileRequest request =
                new CreateTutorProfileRequest(
                        userId,
                        "bio"
                );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> tutorService.createTutorProfile(request)).isInstanceOf(ConflictException.class).hasMessage("User is not a tutor");

        verify(tutorProfileRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTutorProfileAlreadyExists() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .role(UserRole.TUTOR)
                .build();

        CreateTutorProfileRequest request =
                new CreateTutorProfileRequest(
                        userId,
                        "bio"
                );


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(tutorProfileRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> tutorService.createTutorProfile(request)).isInstanceOf(ConflictException.class).hasMessage("Tutor profile already exists");

        verify(tutorProfileRepository, never()).save(any());
    }
}