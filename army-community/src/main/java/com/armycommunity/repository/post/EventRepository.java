package com.armycommunity.repository.post;

import com.armycommunity.model.post.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EventRepository is an interface for managing Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);

    List<Event> findByEventType(String eventType);

    List<Event> findByCreatedBy(Long userId);

    List<Event> findByIsVerifiedTrue();

    List<Event> findByIsVerifiedFalse();

    List<Event> findByIsVerifiedTrueOrderByEventDateAsc();

    long countByIsVerifiedFalse();
}
