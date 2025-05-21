package com.armycommunity.repository.post;

import com.armycommunity.model.post.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);

    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);

    List<Event> findByEventType(String eventType);

    Page<Event> findByCreatedBy(Long userId, Pageable pageable);
}
