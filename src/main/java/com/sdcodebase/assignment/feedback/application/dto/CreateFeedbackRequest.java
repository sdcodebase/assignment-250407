package com.sdcodebase.assignment.feedback.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 피드백 생성 요청. {@code positive=true}는 긍정 피드백, {@code false}는 부정 피드백을 의미한다.
 */
public record CreateFeedbackRequest(
        @NotNull Long chatId,
        @NotNull Boolean positive
) {
}
