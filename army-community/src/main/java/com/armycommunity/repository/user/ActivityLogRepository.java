package com.armycommunity.repository.user;

import com.armycommunity.model.user.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository interface for managing ActivityLog entities.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByUserId(Long userId, Pageable pageable);

    Page<ActivityLog> findByUserIdAndActionType(Long userId, String actionType, Pageable pageable);

    Page<ActivityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :start AND :end")
    Page<ActivityLog> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE a.actionType = :actionType AND a.createdAt BETWEEN :start AND :end")
    Page<ActivityLog> findByActionTypeAndDateRange(
            @Param("actionType") String actionType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}
