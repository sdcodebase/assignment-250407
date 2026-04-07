package com.sdcodebase.assignment.analytics.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    long countByTypeAndCreatedAtGreaterThanEqual(UserActivityType type, Instant since);
}
