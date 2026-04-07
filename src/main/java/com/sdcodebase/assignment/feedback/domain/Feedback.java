package com.sdcodebase.assignment.feedback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "feedbacks", uniqueConstraints = @UniqueConstraint(
        name = "uk_feedbacks_user_chat",
        columnNames = {"user_id", "chat_id"}
))
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "is_positive", nullable = false)
    private boolean positive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    protected Feedback() {
    }

    public Feedback(Long userId, Long chatId, boolean positive) {
        this.userId = userId;
        this.chatId = chatId;
        this.positive = positive;
        this.status = FeedbackStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = FeedbackStatus.PENDING;
        }
    }

    /**
     * 상태 전이. 현재는 단순 setter 형태이나, 향후 전이 규칙(예: RESOLVED → PENDING 금지)이 생기면 이 메서드 안에서 검증한다.
     */
    public void changeStatus(FeedbackStatus next) {
        this.status = next;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getChatId() {
        return chatId;
    }

    public boolean isPositive() {
        return positive;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
