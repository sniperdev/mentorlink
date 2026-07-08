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
import com.sniperdev.mentorlink_backend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorProfileService {

    private final TutorProfileRepository tutorProfileRepository;

    private final TutorProfileMapper tutorProfileMapper;

    private final UserRepository userRepository;

    @Transactional
    public TutorProfileResponse createTutorProfile(CreateTutorProfileRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.TUTOR) {
            throw new ConflictException("User is not a tutor");
        }

        if (tutorProfileRepository.existsByUserId(user.getId())) {
            throw new ConflictException("Tutor profile already exists");
        }

        TutorProfile tutorProfile = TutorProfile.builder()
                .user(user)
                .bio(request.bio())
                .build();

        TutorProfile savedTutorProfile = tutorProfileRepository.save(tutorProfile);

        return tutorProfileMapper.toResponse(savedTutorProfile);
    }
}
