package com.sdcodebase.assignment.chat.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    List<Chat> findByThreadIdInOrderByCreatedAtAsc(Collection<Long> threadIds);

    void deleteByThreadId(Long threadId);

    List<Chat> findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(Instant since);
}
