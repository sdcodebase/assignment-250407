package com.sdcodebase.assignment.analytics.application;

import com.sdcodebase.assignment.analytics.application.dto.ActivityStatsResponse;
import com.sdcodebase.assignment.analytics.domain.AnalyticsAccessDeniedException;
import com.sdcodebase.assignment.analytics.domain.UserActivityLogRepository;
import com.sdcodebase.assignment.analytics.domain.UserActivityType;
import com.sdcodebase.assignment.chat.domain.Chat;
import com.sdcodebase.assignment.chat.domain.ChatRepository;
import com.sdcodebase.assignment.chat.domain.ChatThread;
import com.sdcodebase.assignment.chat.domain.ChatThreadRepository;
import com.sdcodebase.assignment.user.domain.User;
import com.sdcodebase.assignment.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 분석/보고 유스케이스. 모든 메서드는 관리자 권한을 명시적으로 검증한다.
 *
 * <ul>
 *   <li><b>활동 카운트</b>: {@code user_activity_logs}에서 지난 24시간의 가입/로그인/대화 생성 수 집계.</li>
 *   <li><b>대화 보고서(CSV)</b>: 지난 24시간 동안 생성된 모든 대화를 작성 사용자 정보와 함께 CSV로 산출.</li>
 * </ul>
 *
 * <p>리포팅 컨텍스트 특성상 chat/user 등 상위 컨텍스트의 read-only 리포지터리에 의존하지만,
 * 의존 방향은 언제나 {@code analytics → others}로 한 방향이다.
 */
@Service
public class AnalyticsService {

    static final Duration WINDOW = Duration.ofDays(1);

    private final UserActivityLogRepository activityLogRepository;
    private final ChatRepository chatRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final UserRepository userRepository;

    public AnalyticsService(UserActivityLogRepository activityLogRepository,
            ChatRepository chatRepository,
            ChatThreadRepository chatThreadRepository,
            UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.chatRepository = chatRepository;
        this.chatThreadRepository = chatThreadRepository;
        this.userRepository = userRepository;
    }

    /* --------------------------- Activity Stats --------------------------- */

    @Transactional(readOnly = true)
    public ActivityStatsResponse activityStats(boolean isAdmin) {
        requireAdmin(isAdmin);
        Instant until = Instant.now();
        Instant since = until.minus(WINDOW);

        long signups = activityLogRepository
                .countByTypeAndCreatedAtGreaterThanEqual(UserActivityType.SIGNUP, since);
        long logins = activityLogRepository
                .countByTypeAndCreatedAtGreaterThanEqual(UserActivityType.LOGIN, since);
        long chats = activityLogRepository
                .countByTypeAndCreatedAtGreaterThanEqual(UserActivityType.CHAT_CREATED, since);

        return new ActivityStatsResponse(since, until, signups, logins, chats);
    }

    /* ----------------------------- Chat Report ---------------------------- */

    @Transactional(readOnly = true)
    public String generateChatReportCsv(boolean isAdmin) {
        requireAdmin(isAdmin);
        Instant until = Instant.now();
        Instant since = until.minus(WINDOW);

        List<Chat> chats = chatRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(since);

        Set<Long> threadIds = chats.stream().map(Chat::getThreadId).collect(Collectors.toSet());
        Map<Long, ChatThread> threadById = chatThreadRepository.findAllById(threadIds).stream()
                .collect(Collectors.toMap(ChatThread::getId, Function.identity()));

        Set<Long> userIds = threadById.values().stream()
                .map(ChatThread::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        StringBuilder csv = new StringBuilder(1024);
        csv.append("chatId,threadId,userId,userEmail,userName,question,answer,createdAt\n");
        for (Chat chat : chats) {
            ChatThread thread = threadById.get(chat.getThreadId());
            Long userId = thread != null ? thread.getUserId() : null;
            User user = userId != null ? userById.get(userId) : null;

            csv.append(chat.getId()).append(',')
                    .append(chat.getThreadId()).append(',')
                    .append(userId == null ? "" : userId).append(',')
                    .append(csvField(user == null ? null : user.getEmail())).append(',')
                    .append(csvField(user == null ? null : user.getName())).append(',')
                    .append(csvField(chat.getQuestion())).append(',')
                    .append(csvField(chat.getAnswer())).append(',')
                    .append(chat.getCreatedAt())
                    .append('\n');
        }
        return csv.toString();
    }

    /* -------------------------------- Helpers ----------------------------- */

    private static void requireAdmin(boolean isAdmin) {
        if (!isAdmin) {
            throw new AnalyticsAccessDeniedException();
        }
    }

    /**
     * RFC 4180 기본 규칙에 따른 CSV 필드 이스케이프. 콤마/따옴표/개행을 포함하면 따옴표로 감싸고 내부 따옴표는 이중화한다.
     */
    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.indexOf(',') >= 0
                               || value.indexOf('"') >= 0
                               || value.indexOf('\n') >= 0
                               || value.indexOf('\r') >= 0;
        if (!needsQuoting) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
