package com.sdcodebase.assignment.chat.domain;

public class ChatThreadNotFoundException extends RuntimeException {
    public ChatThreadNotFoundException(Long threadId) {
        super("스레드를 찾을 수 없습니다: " + threadId);
    }
}
