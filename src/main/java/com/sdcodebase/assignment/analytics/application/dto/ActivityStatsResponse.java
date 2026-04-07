package com.sdcodebase.assignment.analytics.application.dto;

import java.time.Instant;

/**
 * 지난 24시간 동안의 사용자 활동 카운트.
 *
 * @param since            집계 시작 시점(포함, 호출 시점 - 24h)
 * @param until            집계 종료 시점(호출 시점)
 * @param signupCount      회원가입 수
 * @param loginCount       로그인 성공 수
 * @param chatCreatedCount 대화 생성 수
 */
public record ActivityStatsResponse(
        Instant since,
        Instant until,
        long signupCount,
        long loginCount,
        long chatCreatedCount
) {
}
