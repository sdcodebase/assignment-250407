package com.sdcodebase.assignment.feedback.domain;

public class DuplicateFeedbackException extends RuntimeException {
    public DuplicateFeedbackException(Long userId, Long chatId) {
        super("해당 대화에 이미 피드백이 존재합니다. userId=" + userId + ", chatId=" + chatId);
    }
}
