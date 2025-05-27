package com.armycommunity.service.notification;

import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.user.NotificationResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.NotificationMapper;
import com.armycommunity.model.user.Notification;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.post.NotificationRepository;
import com.armycommunity.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of NotificationService interface for managing notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user ID {} with type: {}", request.getUserId(), request.getType());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.debug("Fetching all notifications for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.debug("Fetching unread notifications for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        return notificationMapper.toResponseList(unreadNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(Long userId) {
        log.debug("Counting unread notification count for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.debug("Marking notification ID {} as read for user ID {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to mark this notification as read");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Successfully marked notification ID: {} as read", notificationId);
        } else {
            log.debug("Notification ID: {} was already marked as read", notificationId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
            log.info("Successfully marked {} notifications as read for user ID: {}",
                    unreadNotifications.size(), userId);
        } else {
            log.debug("No unread notifications found for user ID: {}", userId);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification ID: {} for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Successfully deleted notification ID: {} for user ID: {}", notificationId, userId);
    }

    @Override
    @Transactional
    public void deleteAllUserNotifications(Long userId) {
        log.info("Deleting all notifications for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (!userNotifications.isEmpty()) {
            notificationRepository.deleteAll(userNotifications);
            log.info("Successfully deleted {} notifications for user ID: {}",
                    userNotifications.size(), userId);
        } else {
            log.debug("No notifications found to delete for user ID: {}", userId);
        }
    }

    // Helper methods for common notification types

    /**
     * Creates a like notification
     */
    @Transactional
    public void createLikeNotification(Long postOwnerId, Long likerId, Long postId, String likerUsername) {
        log.debug("Creating like notification for post owner ID: {} from user: {}", postOwnerId, likerUsername);

        // Don't notify users about their own actions
        if (postOwnerId.equals(likerId)) {
            log.debug("Skipping like notification - user liked their own post");
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .userId(postOwnerId)
                .type("LIKE")
                .message(String.format("%s liked your post", likerUsername))
                .relatedEntityId(postId)
                .relatedEntityType("POST")
                .build();

        createNotification(request);
    }

    /**
     * Creates a comment notification
     */
    @Transactional
    public void createCommentNotification(Long postOwnerId, Long commenterId, Long postId, String commenterUsername) {
        log.debug("Creating comment notification for post owner ID: {} from user: {}", postOwnerId, commenterUsername);

        // Don't notify users about their own actions
        if (postOwnerId.equals(commenterId)) {
            log.debug("Skipping comment notification - user commented on their own post");
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .userId(postOwnerId)
                .type("COMMENT")
                .message(String.format("%s commented on your post", commenterUsername))
                .relatedEntityId(postId)
                .relatedEntityType("POST")
                .build();

        createNotification(request);
    }

    /**
     * Creates a follow notification
     */
    @Transactional
    public void createFollowNotification(Long followedUserId, Long followerId, String followerUsername) {
        log.debug("Creating follow notification for user ID: {} from user: {}", followedUserId, followerUsername);

        NotificationRequest request = NotificationRequest.builder()
                .userId(followedUserId)
                .type("FOLLOW")
                .message(String.format("%s started following you", followerUsername))
                .relatedEntityId(followerId)
                .relatedEntityType("USER")
                .build();

        createNotification(request);
    }

    /**
     * Creates an event reminder notification
     */
    @Transactional
    public void createEventReminderNotification(Long userId, Long eventId, String eventTitle) {
        log.debug("Creating event reminder notification for user ID: {} for event: {}", userId, eventTitle);

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type("EVENT_REMINDER")
                .message(String.format("Reminder: %s is coming up soon", eventTitle))
                .relatedEntityId(eventId)
                .relatedEntityType("EVENT")
                .build();

        createNotification(request);
    }

    /**
     * Creates a repost notification
     */
    @Transactional
    public void createRepostNotification(Long postOwnerId, Long reposterId, Long postId, String reposterUsername) {
        log.debug("Creating repost notification for post owner ID: {} from user: {}", postOwnerId, reposterUsername);

        // Don't notify users about their own actions
        if (postOwnerId.equals(reposterId)) {
            log.debug("Skipping repost notification - user reposted their own post");
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .userId(postOwnerId)
                .type("REPOST")
                .message(String.format("%s reposted your post", reposterUsername))
                .relatedEntityId(postId)
                .relatedEntityType("POST")
                .build();

        createNotification(request);
    }
}
