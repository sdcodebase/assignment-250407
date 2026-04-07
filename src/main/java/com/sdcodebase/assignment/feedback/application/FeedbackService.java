package com.sdcodebase.assignment.feedback.application;

import com.sdcodebase.assignment.chat.domain.Chat;
import com.sdcodebase.assignment.chat.domain.ChatNotFoundException;
import com.sdcodebase.assignment.chat.domain.ChatRepository;
import com.sdcodebase.assignment.chat.domain.ChatThread;
import com.sdcodebase.assignment.chat.domain.ChatThreadNotFoundException;
import com.sdcodebase.assignment.chat.domain.ChatThreadRepository;
import com.sdcodebase.assignment.feedback.application.dto.CreateFeedbackRequest;
import com.sdcodebase.assignment.feedback.application.dto.FeedbackResponse;
import com.sdcodebase.assignment.feedback.domain.DuplicateFeedbackException;
import com.sdcodebase.assignment.feedback.domain.Feedback;
import com.sdcodebase.assignment.feedback.domain.FeedbackAccessDeniedException;
import com.sdcodebase.assignment.feedback.domain.FeedbackNotFoundException;
import com.sdcodebase.assignment.feedback.domain.FeedbackRepository;
import com.sdcodebase.assignment.feedback.domain.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 피드백 유스케이스.
 *
 * <ul>
 *   <li>생성: 일반 사용자는 본인이 소유한 대화에만, 관리자는 모든 대화에 가능. 한 사용자는 한 대화에
 *       단 하나의 피드백만 가질 수 있다(DB 유니크 + 사전 체크).</li>
 *   <li>조회: 일반 사용자는 본인의 피드백만, 관리자는 전체. 긍정/부정 필터와 페이지네이션/정렬 지원.</li>
 *   <li>상태 변경: 관리자만 가능.</li>
 * </ul>
 *
 * <p>대화 소유권 검증을 위해 chat 컨텍스트의 read-only 리포지터리에 의존한다. 사용자 도메인은
 * 호출자가 전달한 {@code userId}만 신뢰하며 직접 조회하지 않는다(인증 컨텍스트 위임).
 */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ChatRepository chatRepository;
    private final ChatThreadRepository chatThreadRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
            ChatRepository chatRepository,
            ChatThreadRepository chatThreadRepository) {
        this.feedbackRepository = feedbackRepository;
        this.chatRepository = chatRepository;
        this.chatThreadRepository = chatThreadRepository;
    }

    @Transactional
    public FeedbackResponse createFeedback(Long userId, boolean isAdmin, CreateFeedbackRequest request) {
        Chat chat = chatRepository.findById(request.chatId())
                .orElseThrow(() -> new ChatNotFoundException(request.chatId()));

        if (!isAdmin) {
            ChatThread thread = chatThreadRepository.findById(chat.getThreadId())
                    .orElseThrow(() -> new ChatThreadNotFoundException(chat.getThreadId()));
            if (!Objects.equals(thread.getUserId(), userId)) {
                throw new FeedbackAccessDeniedException(
                        "해당 대화에 피드백을 생성할 권한이 없습니다: chatId=" + request.chatId());
            }
        }

        if (feedbackRepository.existsByUserIdAndChatId(userId, request.chatId())) {
            throw new DuplicateFeedbackException(userId, request.chatId());
        }

        Feedback saved = feedbackRepository.save(
                new Feedback(userId, request.chatId(), request.positive())
        );
        return FeedbackResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> listFeedbacks(Long userId,
            boolean isAdmin,
            Boolean positive,
            Pageable pageable) {
        Page<Feedback> page;
        if (isAdmin) {
            page = (positive == null)
                    ? feedbackRepository.findAll(pageable)
                    : feedbackRepository.findByPositive(positive, pageable);
        } else {
            page = (positive == null)
                    ? feedbackRepository.findByUserId(userId, pageable)
                    : feedbackRepository.findByUserIdAndPositive(userId, positive, pageable);
        }
        return page.map(FeedbackResponse::from);
    }

    @Transactional
    public FeedbackResponse updateStatus(boolean isAdmin, Long feedbackId, FeedbackStatus newStatus) {
        if (!isAdmin) {
            throw new FeedbackAccessDeniedException("관리자만 피드백 상태를 변경할 수 있습니다.");
        }
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
        feedback.changeStatus(newStatus);
        return FeedbackResponse.from(feedback);
    }
}
