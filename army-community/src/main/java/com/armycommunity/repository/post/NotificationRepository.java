package com.armycommunity.repository.post;

import com.armycommunity.model.user.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationRepository is an interface for managing Notification entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    Long countByUserIdAndIsReadFalse(Long userId);
}
