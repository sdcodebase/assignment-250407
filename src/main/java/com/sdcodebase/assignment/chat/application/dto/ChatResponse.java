package com.sdcodebase.assignment.chat.application.dto;

import com.sdcodebase.assignment.chat.domain.Chat;

import java.time.Instant;

public record ChatResponse(
        Long chatId,
        Long threadId,
        String question,
        String answer,
        String model,
        Instant createdAt
) {
    public static ChatResponse of(Chat chat, String model) {
        return new ChatResponse(
                chat.getId(),
                chat.getThreadId(),
                chat.getQuestion(),
                chat.getAnswer(),
                model,
                chat.getCreatedAt()
        );
    }
}
