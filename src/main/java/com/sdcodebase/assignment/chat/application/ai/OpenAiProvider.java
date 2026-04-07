package com.sdcodebase.assignment.chat.application.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * OpenAI provider placeholder. 실제 HTTP 호출은 구현되어 있지 않으며 호출 시 로그만 남긴다.
 * 활성화하려면 {@code @Primary}를 이 클래스로 옮기고 실제 OpenAI Chat Completions API 호출을 추가한다.
 */
@Component
public class OpenAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);
    private static final String NAME = "openai";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public AiCompletion complete(List<AiMessage> messages, String model) {
        log.info("[{}] complete (not implemented): messages={}, model={}", NAME, messages.size(), model);
        return new AiCompletion("(openai placeholder)", model);
    }

    @Override
    public void stream(List<AiMessage> messages, String model, Consumer<String> onChunk) {
        log.info("[{}] stream (not implemented): messages={}, model={}", NAME, messages.size(), model);
        onChunk.accept("(openai placeholder)");
    }
}
