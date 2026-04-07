package com.sdcodebase.assignment.feedback.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByUserIdAndChatId(Long userId, Long chatId);

    Page<Feedback> findByUserId(Long userId, Pageable pageable);

    Page<Feedback> findByPositive(boolean positive, Pageable pageable);

    Page<Feedback> findByUserIdAndPositive(Long userId, boolean positive, Pageable pageable);
}
