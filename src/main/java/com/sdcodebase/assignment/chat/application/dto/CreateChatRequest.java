package com.sdcodebase.assignment.chat.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRequest(
        @NotBlank String question,
        Boolean isStreaming,
        String model
) {
    public boolean streaming() {
        return Boolean.TRUE.equals(isStreaming);
    }
}
