package com.sdcodebase.assignment.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_activity_logs", indexes = {
        @Index(name = "ix_user_activity_logs_type_created_at", columnList = "type, created_at"),
        @Index(name = "ix_user_activity_logs_created_at", columnList = "created_at")
})
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserActivityType type;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    protected UserActivityLog() {
    }

    public UserActivityLog(Long userId, UserActivityType type, Instant createdAt) {
        this.userId = userId;
        this.type = type;
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public UserActivityType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
