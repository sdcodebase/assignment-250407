package com.sdcodebase.assignment.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {

    Optional<ChatThread> findTopByUserIdOrderByLastChatAtDesc(Long userId);

    Page<ChatThread> findByUserId(Long userId, Pageable pageable);
}
