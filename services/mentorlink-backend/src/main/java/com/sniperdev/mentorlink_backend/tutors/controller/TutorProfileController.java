package com.sniperdev.mentorlink_backend.tutors.controller;


import com.sniperdev.mentorlink_backend.tutors.dto.CreateTutorProfileRequest;
import com.sniperdev.mentorlink_backend.tutors.dto.TutorProfileResponse;
import com.sniperdev.mentorlink_backend.tutors.service.TutorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;

    @PostMapping("/profile")
    public ResponseEntity<TutorProfileResponse> createTutorProfile (@Valid @RequestBody CreateTutorProfileRequest request) {
        TutorProfileResponse response = tutorProfileService.createTutorProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
