package com.sdcodebase.assignment.chat.application.ai;

import java.util.List;
import java.util.function.Consumer;

/**
 * 챗봇 응답 생성을 위한 외부 AI 백엔드 추상화.
 * <p>
 * 구현체는 OpenAI, Anthropic 등 어떤 provider라도 가능하며, 기본 구현으로
 * {@link StubAiProvider}를 제공해 외부 API 키 없이도 시연 가능하다.
 */
public interface AiProvider {

    /** Provider 식별자 (예: "stub", "openai") */
    String name();

    /** 동기 응답 생성. */
    AiCompletion complete(List<AiMessage> messages, String model);

    /**
     * 스트리밍 응답 생성. 청크가 생성될 때마다 {@code onChunk}로 전달된다.
     * 호출자는 스레드 안전성을 유지해야 한다.
     */
    void stream(List<AiMessage> messages, String model, Consumer<String> onChunk);
}
