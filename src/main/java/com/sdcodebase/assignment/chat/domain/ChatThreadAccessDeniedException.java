package com.sdcodebase.assignment.chat.domain;

public class ChatThreadAccessDeniedException extends RuntimeException {
    public ChatThreadAccessDeniedException(Long threadId) {
        super("해당 스레드에 접근할 권한이 없습니다: " + threadId);
    }
}
