package com.zyvora.zyvora_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    private String picture;

    // GOOGLE, GITHUB, LINKEDIN, LOCAL (for future email/password)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    // The unique ID Google/GitHub gives this user (their "sub" claim)
    @Column(name = "provider_id")
    private String providerId;

    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "readiness_score")
    @Builder.Default
    private Integer readinessScore = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
    }

    public enum AuthProvider {
        GOOGLE, GITHUB, LINKEDIN, LOCAL
    }
}