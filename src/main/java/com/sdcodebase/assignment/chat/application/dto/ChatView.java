package com.sdcodebase.assignment.chat.application.dto;

import com.sdcodebase.assignment.chat.domain.Chat;

import java.time.Instant;

public record ChatView(
        Long id,
        String question,
        String answer,
        Instant createdAt
) {
    public static ChatView from(Chat chat) {
        return new ChatView(chat.getId(), chat.getQuestion(), chat.getAnswer(), chat.getCreatedAt());
    }
}
