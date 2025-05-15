package com.armycommunity.service.notification;

import com.armycommunity.dto.response.user.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    public void createNotification(Long userId, String type, String message, Long relatedEntityId, String relatedEntityType);

    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    Integer getUnreadNotificationCount(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId);
}
