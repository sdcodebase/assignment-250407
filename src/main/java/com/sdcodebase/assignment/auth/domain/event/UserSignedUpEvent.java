package com.sdcodebase.assignment.auth.domain.event;

/**
 * 신규 회원가입 발생 시 발행되는 도메인 이벤트.
 *
 * <p>{@code analytics} 컨텍스트의 리스너가 활동 로그로 적재한다. 발행자({@code auth})는 컨슈머의
 * 존재를 알지 못하며, 의존 방향은 항상 {@code analytics → auth}로 한 방향이다.
 */
public record UserSignedUpEvent(Long userId) {
}
