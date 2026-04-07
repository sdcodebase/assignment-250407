package com.sdcodebase.assignment.auth.domain.event;

/**
 * 로그인 성공 시 발행되는 도메인 이벤트. 실패 로그인은 발행되지 않는다.
 */
public record UserLoggedInEvent(Long userId) {
}
