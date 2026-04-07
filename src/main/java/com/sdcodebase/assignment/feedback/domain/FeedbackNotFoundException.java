package com.sdcodebase.assignment.feedback.domain;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(Long feedbackId) {
        super("피드백을 찾을 수 없습니다: " + feedbackId);
    }
}
