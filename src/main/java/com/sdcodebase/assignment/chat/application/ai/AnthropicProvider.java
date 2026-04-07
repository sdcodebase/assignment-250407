package com.sdcodebase.assignment.chat.application.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Anthropic provider placeholder. 실제 HTTP 호출은 구현되어 있지 않으며 호출 시 로그만 남긴다.
 * 활성화 시 {@code @Primary}를 이 클래스로 옮기고 Anthropic Messages API 호출을 추가한다.
 */
@Component
public class AnthropicProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);
    private static final String NAME = "anthropic";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public AiCompletion complete(List<AiMessage> messages, String model) {
        log.info("[{}] complete (not implemented): messages={}, model={}", NAME, messages.size(), model);
        return new AiCompletion("(anthropic placeholder)", model);
    }

    @Override
    public void stream(List<AiMessage> messages, String model, Consumer<String> onChunk) {
        log.info("[{}] stream (not implemented): messages={}, model={}", NAME, messages.size(), model);
        onChunk.accept("(anthropic placeholder)");
    }
}
