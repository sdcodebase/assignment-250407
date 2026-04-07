package com.sdcodebase.assignment.chat.resource;

import com.sdcodebase.assignment.auth.domain.AuthenticatedUser;
import com.sdcodebase.assignment.chat.application.ChatService;
import com.sdcodebase.assignment.chat.application.dto.CreateChatRequest;
import com.sdcodebase.assignment.chat.application.dto.ThreadView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 대화 생성. {@code isStreaming=true}이면 SSE로 응답하고, 아니면 단건 JSON 응답.
     * 단일 엔드포인트로 두 모드를 모두 처리하기 위해 반환 타입은 {@link Object}.
     */
    @PostMapping(value = "/chats", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object createChat(@AuthenticationPrincipal AuthenticatedUser user,
                             @Valid @RequestBody CreateChatRequest request) {
        if (request.streaming()) {
            return chatService.createChatStream(user.id(), request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChat(user.id(), request));
    }

    @GetMapping("/chats")
    public Page<ThreadView> listChats(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                      Pageable pageable) {
        return chatService.listThreads(user.id(), user.isAdmin(), pageable);
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<Void> deleteThread(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable Long threadId) {
        chatService.deleteThread(user.id(), threadId);
        return ResponseEntity.noContent().build();
    }

}
