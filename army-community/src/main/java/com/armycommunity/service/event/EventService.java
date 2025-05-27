package com.armycommunity.service.event;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventResponse createEvent(Long userId, EventRequest request);

    EventResponse getEventById(Long eventId);

    EventResponse updateEvent(Long eventId, Long userId, EventRequest request);

    void deleteEvent(Long eventId, Long userId);

    List<EventResponse> getUpcomingEvents(int limit);

    List<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<EventResponse> getEventsByType(String eventType);

    List<EventResponse> getVerifiedEvents();

    List<EventResponse> getPendingVerificationEvents();

    List<EventResponse> getUserEvents(Long userId);

    List<EventResponse> getAllEvents();

    EventResponse verifyEvent(Long eventId, Long verifierId);

    Long getPendingVerificationCount();
}
