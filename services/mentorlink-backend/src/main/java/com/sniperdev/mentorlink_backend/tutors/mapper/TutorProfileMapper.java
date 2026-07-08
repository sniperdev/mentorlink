package com.sniperdev.mentorlink_backend.tutors.mapper;

import com.sniperdev.mentorlink_backend.tutors.dto.TutorProfileResponse;
import com.sniperdev.mentorlink_backend.tutors.model.TutorProfile;
import org.springframework.stereotype.Component;

@Component
public class TutorProfileMapper {

    public TutorProfileResponse toResponse(TutorProfile tutorProfile) {
        return new TutorProfileResponse(
                tutorProfile.getId(),
                tutorProfile.getUser().getId(),
                tutorProfile.getBio(),
                tutorProfile.isActive(),
                tutorProfile.getCreatedAt(),
                tutorProfile.getUpdatedAt()
        );
    }
}
