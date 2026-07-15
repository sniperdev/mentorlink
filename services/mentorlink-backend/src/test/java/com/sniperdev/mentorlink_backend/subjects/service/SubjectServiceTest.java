package com.sniperdev.mentorlink_backend.subjects.service;

import com.sniperdev.mentorlink_backend.common.exception.ConflictException;
import com.sniperdev.mentorlink_backend.subjects.dto.CreateSubjectRequest;
import com.sniperdev.mentorlink_backend.subjects.dto.SubjectResponse;
import com.sniperdev.mentorlink_backend.subjects.mapper.SubjectMapper;
import com.sniperdev.mentorlink_backend.subjects.model.Subject;
import com.sniperdev.mentorlink_backend.subjects.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private SubjectService subjectService;

    @Test
    void shouldCreateSubject() {
        // given
        CreateSubjectRequest request = new CreateSubjectRequest("Math");

        Subject savedSubject = Subject.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .slug("math")
                .build();

        SubjectResponse subjectResponse =
                new SubjectResponse(
                        savedSubject.getId(),
                        savedSubject.getName(),
                        savedSubject.getSlug(),
                        savedSubject.isActive(),
                        Instant.now(),
                        Instant.now()
                );

        when(subjectMapper.toResponse(savedSubject)).thenReturn(subjectResponse);

        when(subjectRepository.existsByNameIgnoreCase(request.name())).thenReturn(false);

        when(subjectRepository.existsBySlug("math")).thenReturn(false);

        when(subjectRepository.save(any(Subject.class))).thenReturn(savedSubject);

        // when

        SubjectResponse response = subjectService.createSubject(request);

        // then

        assertThat(response.name()).isEqualTo("Math");
        assertThat(response.slug()).isEqualTo("math");

        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void shouldThrowExceptionWhenSubjectAlreadyExists() {
        CreateSubjectRequest request = new CreateSubjectRequest("Math");

        when(subjectRepository.existsByNameIgnoreCase(request.name())).thenReturn(true);

        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Subject with name 'Math' already exists");

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldThrowExceptionWhenSubjectSlugAlreadyExists() {
        CreateSubjectRequest request = new CreateSubjectRequest("Math");

        when(subjectRepository.existsBySlug("math")).thenReturn(true);

        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Generated slug already exists");

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldReturnAllSubjects() {
        Subject subject1 = Subject.builder()
                .id(UUID.randomUUID())
                .name("Math")
                .slug("math")
                .build();

        Subject subject2 = Subject.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .slug("physics")
                .build();

        Instant now = Instant.now();

        SubjectResponse response1 = new SubjectResponse(
                subject1.getId(),
                subject1.getName(),
                subject1.getSlug(),
                true,
                now,
                now
        );

        SubjectResponse response2 = new SubjectResponse(
                subject2.getId(),
                subject2.getName(),
                subject2.getSlug(),
                true,
                now,
                now
        );

        when(subjectRepository.findAll()).thenReturn(List.of(subject1, subject2));
        when(subjectMapper.toResponse(subject1)).thenReturn(response1);
        when(subjectMapper.toResponse(subject2)).thenReturn(response2);

        List<SubjectResponse> result = subjectService.getAllSubjects();

        assertThat(result).hasSize(2);

        assertThat(result.get(0).name())
                .isEqualTo("Math");

        assertThat(result.get(1).name())
                .isEqualTo("Physics");
    }

    @Test
    void shouldReturnSubjectById() {
        // Arrange
        UUID id = UUID.randomUUID();

        Subject subject = Subject.builder()
                .id(id)
                .name("Math")
                .slug("math")
                .build();

        Instant now = Instant.now();

        SubjectResponse expectedResponse = new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getSlug(),
                subject.isActive(),
                now,
                now
        );

        when(subjectRepository.findById(id))
                .thenReturn(Optional.of(subject));

        when(subjectMapper.toResponse(subject))
                .thenReturn(expectedResponse);

        // Act
        SubjectResponse response = subjectService.getSubjectById(id);

        // Assert
        assertThat(response.name()).isEqualTo("Math");
        assertThat(response.slug()).isEqualTo("math");

        verify(subjectRepository).findById(id);
        verify(subjectMapper).toResponse(subject);
    }
}