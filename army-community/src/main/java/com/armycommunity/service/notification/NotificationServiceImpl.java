package com.armycommunity.service.notification;

import com.armycommunity.dto.response.user.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    @Override
    public void createNotification(Long userId, String type, String message, Long relatedEntityId, String relatedEntityType) {
        // TODO: implement
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        // TODO: implement
        return null;
    }

    @Override
    public Integer getUnreadNotificationCount(Long userId) {
        // TODO: implement
        return null;
    }

    @Override
    public void markAsRead(Long notificationId) {
        // TODO: implement
    }

    @Override
    public void markAllAsRead(Long userId) {
        // TODO: implement
    }

    @Override
    public void deleteNotification(Long notificationId) {
        // TODO: implement
    }
}
