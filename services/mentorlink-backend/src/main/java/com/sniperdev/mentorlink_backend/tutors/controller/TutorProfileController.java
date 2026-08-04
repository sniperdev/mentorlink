package com.sniperdev.mentorlink_backend.tutors.controller;


import com.sniperdev.mentorlink_backend.tutors.dto.CreateTutorProfileRequest;
import com.sniperdev.mentorlink_backend.tutors.dto.TutorProfileResponse;
import com.sniperdev.mentorlink_backend.tutors.dto.TutorSummaryResponse;
import com.sniperdev.mentorlink_backend.tutors.service.TutorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TutorProfileResponse> createTutorProfile(@Valid @RequestBody CreateTutorProfileRequest request) {
        TutorProfileResponse response = tutorProfileService.createTutorProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TutorSummaryResponse>> getAllTutors() {
        return ResponseEntity.ok(tutorProfileService.getAllTutors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorSummaryResponse> getTutorProfile(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                tutorProfileService.getTutorProfile(id)
        );
    }
}
