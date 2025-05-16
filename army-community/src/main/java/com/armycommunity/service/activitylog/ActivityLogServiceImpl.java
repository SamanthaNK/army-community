package com.armycommunity.service.activitylog;

import com.armycommunity.model.user.ActivityLog;
import com.armycommunity.repository.user.ActivityLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, Map<String, Object> details) {
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
    @Transactional
    public ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, String ipAddress, String userAgent) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUserId(userId);
        activityLog.setActionType(actionType);
        activityLog.setEntityType(entityType);
        activityLog.setEntityId(entityId);
        activityLog.setIpAddress(ipAddress);
        activityLog.setUserAgent(userAgent);
        activityLog.setCreatedAt(LocalDateTime.now());

        try {
            Map<String, Object> details = new HashMap<>();
            if (ipAddress != null) {
                details.put("ipAddress", ipAddress);
            }
            if (userAgent != null) {
                details.put("userAgent", userAgent);
            }
            activityLog.setDetails(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            System.err.println("Error converting details to JSON string: " + e.getMessage());
            activityLog.setDetails("{}");
        }

        return activityLogRepository.save(activityLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivities(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivitiesByType(Long userId, String actionType, Pageable pageable) {
        return activityLogRepository.findByUserIdAndActionType(userId, actionType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivitiesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return activityLogRepository.findByCreatedAtBetween(start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getRecentActivities(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getUserActivitiesByDateRange(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return activityLogRepository.findByUserIdAndDateRange(userId, start, end, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivitiesByTypeAndDateRange(String actionType, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return activityLogRepository.findByActionTypeAndDateRange(actionType, start, end, pageable);
    }
}
