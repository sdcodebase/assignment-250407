package com.sdcodebase.assignment.chat.domain;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(Long chatId) {
        super("대화를 찾을 수 없습니다: " + chatId);
    }
}
