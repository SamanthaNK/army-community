package com.armycommunity.service.notification;

import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.user.NotificationResponse;

import java.util.List;

public interface NotificationService {

    public NotificationResponse createNotification(NotificationRequest request);

    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    Long getUnreadNotificationCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    void deleteAllUserNotifications(Long userId);

    void createLikeNotification(Long postOwnerId, Long likerId, Long postId, String likerUsername);

    void createCommentNotification(Long postOwnerId, Long commenterId, Long postId, String commenterUsername);

    void createFollowNotification(Long followedUserId, Long followerId, String followerUsername);

    void createEventReminderNotification(Long userId, Long eventId, String eventTitle);

    void createRepostNotification(Long postOwnerId, Long reposterId, Long postId, String reposterUsername);
}
