package com.sdcodebase.assignment.chat.application;

import com.sdcodebase.assignment.chat.application.ai.AiCompletion;
import com.sdcodebase.assignment.chat.application.ai.AiMessage;
import com.sdcodebase.assignment.chat.application.ai.AiProvider;
import com.sdcodebase.assignment.chat.application.dto.ChatResponse;
import com.sdcodebase.assignment.chat.application.dto.CreateChatRequest;
import com.sdcodebase.assignment.chat.application.dto.ThreadView;
import com.sdcodebase.assignment.chat.domain.Chat;
import com.sdcodebase.assignment.chat.domain.ChatRepository;
import com.sdcodebase.assignment.chat.domain.ChatThread;
import com.sdcodebase.assignment.chat.domain.ChatThreadAccessDeniedException;
import com.sdcodebase.assignment.chat.domain.ChatThreadNotFoundException;
import com.sdcodebase.assignment.chat.domain.ChatThreadRepository;
import com.sdcodebase.assignment.chat.domain.event.ChatCreatedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 챗봇 대화 유스케이스. 스레드 30분 규칙, 동기/스트리밍 응답 생성, 권한 분기 조회, 스레드 삭제를 담당한다. 호출자는 인증 결과로부터 얻은 {@code userId}를 직접 전달해야 하며, 본 서비스는 사용자 도메인을 다시 조회하지 않는다.
 */
@Service
public class ChatService {

    static final Duration THREAD_GAP = Duration.ofMinutes(30);
    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final ChatThreadRepository threadRepository;
    private final ChatRepository chatRepository;
    private final AiProvider aiProvider;
    private final Executor sseExecutor;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ChatService(ChatThreadRepository threadRepository,
            ChatRepository chatRepository,
            AiProvider aiProvider,
            @Qualifier("chatSseExecutor") Executor sseExecutor,
            TransactionTemplate transactionTemplate,
            ApplicationEventPublisher eventPublisher) {
        this.threadRepository = threadRepository;
        this.chatRepository = chatRepository;
        this.aiProvider = aiProvider;
        this.sseExecutor = sseExecutor;
        this.transactionTemplate = transactionTemplate;
        this.eventPublisher = eventPublisher;
    }

    /* ----------------------------- Create ----------------------------- */

    @Transactional
    public ChatResponse createChat(Long userId, CreateChatRequest request) {
        ChatThread thread = resolveThread(userId);
        List<AiMessage> history = buildHistory(thread.getId());
        history.add(AiMessage.user(request.question()));

        AiCompletion completion = aiProvider.complete(history, request.model());
        Chat chat = chatRepository.save(new Chat(thread.getId(), request.question(), completion.content()));
        thread.touch(chat.getCreatedAt());
        return ChatResponse.of(chat, completion.model());
    }

    /**
     * 스트리밍 응답. 요청 스레드에서 스레드 해석/이력 로드를 마치고, SSE 송신과 채팅 저장을 별도 스레드(executor)에서 수행한다. SSE 비동기 작업 안에서는 {@link TransactionTemplate}로 트랜잭션 경계를 명시적으로 연다.
     */
    public SseEmitter createChatStream(Long userId, CreateChatRequest request) {
        StreamingPlan plan = transactionTemplate.execute(status -> {
            ChatThread thread = resolveThread(userId);
            List<AiMessage> history = buildHistory(thread.getId());
            history.add(AiMessage.user(request.question()));
            return new StreamingPlan(thread.getId(), request.question(), request.model(), history);
        });
        Objects.requireNonNull(plan, "streaming plan must not be null");

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitter.onTimeout(emitter::complete);
        emitter.onError(t -> emitter.complete());

        sseExecutor.execute(() -> runStream(plan, emitter));
        return emitter;
    }

    private void runStream(StreamingPlan plan, SseEmitter emitter) {
        StringBuilder buffer = new StringBuilder();
        try {
            sendEvent(emitter, "thread", Map.of("threadId", plan.threadId()));
            aiProvider.stream(plan.history(), plan.model(), chunk -> {
                buffer.append(chunk);
                sendEvent(emitter, "chunk", Map.of("delta", chunk));
            });

            String answer = buffer.toString();
            ChatResponse saved = transactionTemplate.execute(status -> {
                Chat chat = chatRepository.save(new Chat(plan.threadId(), plan.question(), answer));
                ChatThread thread = threadRepository.findById(plan.threadId())
                        .orElseThrow(() -> new ChatThreadNotFoundException(plan.threadId()));
                thread.touch(chat.getCreatedAt());
                eventPublisher.publishEvent(new ChatCreatedEvent(
                        thread.getUserId(), chat.getId(), thread.getId()));
                return ChatResponse.of(chat, plan.model() == null ? "" : plan.model());
            });

            sendEvent(emitter, "done", saved);
            emitter.complete();
        } catch (Exception e) {
            try {
                sendEvent(emitter, "error", Map.of("message", e.getMessage() == null ? "stream failed" : e.getMessage()));
            } catch (Exception ignored) {
                // emitter already broken
            }
            emitter.completeWithError(e);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* ------------------------------- List ----------------------------- */

    @Transactional(readOnly = true)
    public Page<ThreadView> listThreads(Long userId, boolean isAdmin, Pageable pageable) {
        Page<ChatThread> threads = isAdmin
                ? threadRepository.findAll(pageable)
                : threadRepository.findByUserId(userId, pageable);

        Map<Long, List<Chat>> chatsByThread = loadChats(threads.getContent());
        return threads.map(thread -> ThreadView.from(
                thread,
                chatsByThread.getOrDefault(thread.getId(), List.of())
        ));
    }

    private Map<Long, List<Chat>> loadChats(Collection<ChatThread> threads) {
        if (threads.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = threads.stream().map(ChatThread::getId).toList();
        return chatRepository.findByThreadIdInOrderByCreatedAtAsc(ids).stream()
                .collect(Collectors.groupingBy(Chat::getThreadId));
    }

    /* ------------------------------ Delete ---------------------------- */

    @Transactional
    public void deleteThread(Long userId, Long threadId) {
        ChatThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ChatThreadNotFoundException(threadId));
        if (!Objects.equals(thread.getUserId(), userId)) {
            throw new ChatThreadAccessDeniedException(threadId);
        }
        chatRepository.deleteByThreadId(threadId);
        threadRepository.delete(thread);
    }

    /* ----------------------------- Helpers ---------------------------- */

    private ChatThread resolveThread(Long userId) {
        Instant now = Instant.now();
        Optional<ChatThread> latest = threadRepository.findTopByUserIdOrderByLastChatAtDesc(userId);
        if (latest.isPresent()
            && Duration.between(latest.get().getLastChatAt(), now).compareTo(THREAD_GAP) <= 0) {
            return latest.get();
        }
        return threadRepository.save(new ChatThread(userId));
    }

    private List<AiMessage> buildHistory(Long threadId) {
        List<Chat> existing = chatRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
        existing.sort(Comparator.comparing(Chat::getCreatedAt));
        List<AiMessage> history = new ArrayList<>(existing.size() * 2);
        for (Chat c : existing) {
            history.add(AiMessage.user(c.getQuestion()));
            history.add(AiMessage.assistant(c.getAnswer()));
        }
        return history;
    }

    private record StreamingPlan(Long threadId, String question, String model, List<AiMessage> history) {

    }
}
