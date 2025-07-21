package com.armycommunity.service.activitylog;

import com.armycommunity.model.user.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface ActivityLogService {

    ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, Map<String, Object> details);

    Page<ActivityLog> getUserActivities(Long userId, Pageable pageable);

    Page<ActivityLog> getUserActivitiesByType(Long userId, String actionType, Pageable pageable);

    Page<ActivityLog> getActivitiesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<ActivityLog> getRecentActivities(Pageable pageable);

    Page<ActivityLog> getUserActivitiesByDateRange(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<ActivityLog> getActivitiesByTypeAndDateRange(String actionType, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
