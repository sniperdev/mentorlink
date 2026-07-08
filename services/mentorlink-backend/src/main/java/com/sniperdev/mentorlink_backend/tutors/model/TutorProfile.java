package com.sniperdev.mentorlink_backend.tutors.model;

import com.sniperdev.mentorlink_backend.common.audit.AuditableEntity;
import com.sniperdev.mentorlink_backend.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "tutor_profiles")
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    private boolean active = true;
}

