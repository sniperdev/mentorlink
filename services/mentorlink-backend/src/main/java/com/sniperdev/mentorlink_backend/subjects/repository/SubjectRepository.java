package com.sniperdev.mentorlink_backend.subjects.repository;

import com.sniperdev.mentorlink_backend.subjects.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlug(String name);
}
