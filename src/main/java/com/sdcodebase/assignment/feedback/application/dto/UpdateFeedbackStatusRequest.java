package com.sdcodebase.assignment.feedback.application.dto;

import com.sdcodebase.assignment.feedback.domain.FeedbackStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFeedbackStatusRequest(
        @NotNull FeedbackStatus status
) {
}
