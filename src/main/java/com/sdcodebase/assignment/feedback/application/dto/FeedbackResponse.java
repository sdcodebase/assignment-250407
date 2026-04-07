package com.sdcodebase.assignment.feedback.application.dto;

import com.sdcodebase.assignment.feedback.domain.Feedback;
import com.sdcodebase.assignment.feedback.domain.FeedbackStatus;

import java.time.Instant;

public record FeedbackResponse(
        Long id,
        Long userId,
        Long chatId,
        boolean positive,
        FeedbackStatus status,
        Instant createdAt
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getUserId(),
                feedback.getChatId(),
                feedback.isPositive(),
                feedback.getStatus(),
                feedback.getCreatedAt()
        );
    }
}
