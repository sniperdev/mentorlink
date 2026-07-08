package com.sniperdev.mentorlink_backend.tutors.repository;

import com.sniperdev.mentorlink_backend.tutors.model.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, UUID> {

    boolean existsByUserId(UUID userId);
}
