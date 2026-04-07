package com.sdcodebase.assignment.chat.application.dto;

import com.sdcodebase.assignment.chat.domain.Chat;
import com.sdcodebase.assignment.chat.domain.ChatThread;

import java.time.Instant;
import java.util.List;

public record ThreadView(
        Long id,
        Long userId,
        Instant createdAt,
        Instant lastChatAt,
        List<ChatView> chats
) {
    public static ThreadView from(ChatThread thread, List<Chat> chats) {
        return new ThreadView(
                thread.getId(),
                thread.getUserId(),
                thread.getCreatedAt(),
                thread.getLastChatAt(),
                chats.stream().map(ChatView::from).toList()
        );
    }
}
