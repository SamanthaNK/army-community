package com.armycommunity.service.activitylog;

import com.armycommunity.model.user.ActivityLog;
import com.armycommunity.repository.user.ActivityLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, Map<String, Object> details) {
        log.info("Logging activity: {} - {} - {} for user {}", actionType, entityType, entityId, userId);

        ActivityLog activityLog = new ActivityLog();
        activityLog.setUserId(userId);
        activityLog.setActionType(actionType);
        activityLog.setEntityType(entityType);
        activityLog.setEntityId(entityId);
        activityLog.setCreatedAt(LocalDateTime.now());

        if (details != null) {
            // Extract IP and user agent if they exist in the details
            if (details.containsKey("ipAddress") && details.get("ipAddress") != null) {
                activityLog.setIpAddress(details.get("ipAddress").toString());
            }

            if (details.containsKey("userAgent") && details.get("userAgent") != null) {
                activityLog.setUserAgent(details.get("userAgent").toString());
            }

            try {
                // Convert map to JSON string
                activityLog.setDetails(objectMapper.writeValueAsString(details));
            } catch (JsonProcessingException e) {
                System.err.println("Error converting details to JSON string: " + e.getMessage());
                // Set a simple JSON object as fallback
                activityLog.setDetails("{}");
            }
        } else {
            activityLog.setDetails("{}");
        }

        return activityLogRepository.save(activityLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivities(Long userId, Pageable pageable) {
        log.info("Retrieving activities for user {}", userId);
        return activityLogRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivitiesByType(Long userId, String actionType, Pageable pageable) {
        log.info("Retrieving {} activities for user: {}", actionType, userId);
        return activityLogRepository.findByUserIdAndActionType(userId, actionType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivitiesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.info("Retrieving activities between {} and {}", start, end);
        return activityLogRepository.findByCreatedAtBetween(start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getRecentActivities(Pageable pageable) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        log.debug("Retrieving recent activities since {}", twentyFourHoursAgo);
        return activityLogRepository.findByCreatedAtBetween(twentyFourHoursAgo, LocalDateTime.now(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivitiesByDateRange(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.info("Retrieving activities for user {} between {} and {}", userId, start, end);
        return activityLogRepository.findByUserIdAndDateRange(userId, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivitiesByTypeAndDateRange(String actionType, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.info("Retrieving {} activities between {} and {}", actionType, start, end);
        return activityLogRepository.findByActionTypeAndDateRange(actionType, start, end, pageable);
    }
}
