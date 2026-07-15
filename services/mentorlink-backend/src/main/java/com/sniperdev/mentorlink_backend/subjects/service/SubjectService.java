package com.sniperdev.mentorlink_backend.subjects.service;

import com.sniperdev.mentorlink_backend.common.exception.ConflictException;
import com.sniperdev.mentorlink_backend.common.exception.ResourceNotFoundException;
import com.sniperdev.mentorlink_backend.subjects.dto.CreateSubjectRequest;
import com.sniperdev.mentorlink_backend.subjects.dto.SubjectResponse;
import com.sniperdev.mentorlink_backend.subjects.mapper.SubjectMapper;
import com.sniperdev.mentorlink_backend.subjects.model.Subject;
import com.sniperdev.mentorlink_backend.subjects.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    private final SubjectMapper mapper;

    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {

        String normalizedName = request.name().trim();

        if (subjectRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("Subject with name '" + request.name() + "' already exists");
        }

        String slug = generateSlug(request.name());

        if (subjectRepository.existsBySlug(slug)) {
            throw new ConflictException("Generated slug already exists");
        }

        Subject subject = Subject.builder()
                .name(request.name())
                .slug(slug)
                .build();

        Subject savedSubject = subjectRepository.save(subject);

        return mapper.toResponse(savedSubject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return  subjectRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return mapper.toResponse(subject);
    }

    private String generateSlug(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace(" ", "-");
    }
}