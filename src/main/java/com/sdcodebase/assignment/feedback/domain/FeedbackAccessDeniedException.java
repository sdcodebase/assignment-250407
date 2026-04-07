package com.sdcodebase.assignment.feedback.domain;

public class FeedbackAccessDeniedException extends RuntimeException {
    public FeedbackAccessDeniedException(String message) {
        super(message);
    }
}
