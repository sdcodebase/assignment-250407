package com.sdcodebase.assignment.feedback.domain;

/**
 * 피드백 처리 상태. 신규 생성 시 {@link #PENDING}, 관리자가 처리 완료 표시 시 {@link #RESOLVED}.
 */
public enum FeedbackStatus {
    PENDING,
    RESOLVED
}
