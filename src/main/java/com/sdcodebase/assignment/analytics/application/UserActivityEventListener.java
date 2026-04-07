package com.sdcodebase.assignment.analytics.application;

import com.sdcodebase.assignment.analytics.domain.UserActivityLog;
import com.sdcodebase.assignment.analytics.domain.UserActivityLogRepository;
import com.sdcodebase.assignment.analytics.domain.UserActivityType;
import com.sdcodebase.assignment.auth.domain.event.UserLoggedInEvent;
import com.sdcodebase.assignment.auth.domain.event.UserSignedUpEvent;
import com.sdcodebase.assignment.chat.domain.event.ChatCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트를 받아 사용자 활동 로그로 적재하는 리스너.
 *
 * <p>모든 핸들러는 {@link TransactionalEventListener}로 발행자 트랜잭션의 <b>커밋 직후</b>에 실행되며,
 * {@link Propagation#REQUIRES_NEW}로 별도 트랜잭션을 연다. 이로써 로그 적재 실패가 원본 도메인 트랜잭션(가입/로그인/대화 생성)을 절대 롤백하지 못한다.
 */
@Component
public class UserActivityEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserActivityEventListener.class);

    private final UserActivityLogRepository repository;

    public UserActivityEventListener(UserActivityLogRepository repository) {
        this.repository = repository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserSignedUp(UserSignedUpEvent event) {
        record(event.userId(), UserActivityType.SIGNUP);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserLoggedIn(UserLoggedInEvent event) {
        record(event.userId(), UserActivityType.LOGIN);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onChatCreated(ChatCreatedEvent event) {
        record(event.userId(), UserActivityType.CHAT_CREATED);
    }

    private void record(Long userId, UserActivityType type) {
        try {
            repository.save(new UserActivityLog(userId, type, java.time.Instant.now()));
        } catch (RuntimeException ex) {
            // 도메인 동작에 영향이 없도록 로그만 남기고 삼킨다.
            log.warn("Failed to record user activity log: type={}, userId={}, error={}",
                    type, userId, ex.getMessage());
        }
    }
}
