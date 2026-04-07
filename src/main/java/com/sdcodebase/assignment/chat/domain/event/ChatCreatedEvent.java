package com.sdcodebase.assignment.chat.domain.event;

/**
 * 대화 한 건이 생성되어 영속화된 시점에 발행되는 도메인 이벤트.
 * 동기 응답과 SSE 스트리밍 응답 양쪽 경로 모두에서 발행된다.
 */
public record ChatCreatedEvent(Long userId, Long chatId, Long threadId) {
}
