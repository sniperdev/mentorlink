package com.sniperdev.mentorlink_backend.subjects.mapper;

import com.sniperdev.mentorlink_backend.subjects.dto.SubjectResponse;
import com.sniperdev.mentorlink_backend.subjects.model.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {

    public SubjectResponse toResponse(Subject subject) {
        return  new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getSlug(),
                subject.isActive(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
