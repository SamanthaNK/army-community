package com.armycommunity.service.event;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EventService {
    EventResponse createEvent(Long userId, EventRequest request);

    EventResponse getEventById(Long eventId);

    EventResponse updateEvent(Long eventId, Long userId, EventRequest request);

    void deleteEvent(Long eventId, Long userId);

    List<EventResponse> getUpcomingEvents(int limit);

    List<EventResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate);

    List<EventResponse> getEventsByType(String eventType);

    Page<EventResponse> getUserEvents(Long userId, Pageable pageable);

    Page<EventResponse> getAllEvents(Pageable pageable);
}
