package com.armycommunity.repository.user;

import com.armycommunity.model.user.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);

    List<ActivityLog> findByActionType(String actionType);

    List<ActivityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<ActivityLog> findByUserIdAndActionType(Long userId, String actionType, Pageable pageable);
}
