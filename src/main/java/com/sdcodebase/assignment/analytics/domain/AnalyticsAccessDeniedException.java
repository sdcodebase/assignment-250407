package com.sdcodebase.assignment.analytics.domain;

public class AnalyticsAccessDeniedException extends RuntimeException {

    public AnalyticsAccessDeniedException() {
        super("관리자만 분석/보고 기능을 사용할 수 있습니다.");
    }
}
