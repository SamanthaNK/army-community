package com.armycommunity.service.event;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    @Override
    public EventResponse createEvent(Long userId, EventRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public EventResponse getEventById(Long eventId) {
        // TODO: implement
        return null;
    }

    @Override
    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteEvent(Long eventId, Long userId) {
        // TODO: implement
    }

    @Override
    public List<EventResponse> getUpcomingEvents(int limit) {
        // TODO: implement
        return null;
    }

    @Override
    public List<EventResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        // TODO: implement
        return null;
    }

    @Override
    public List<EventResponse> getEventsByType(String eventType) {
        // TODO: implement
        return null;
    }

    @Override
    public Page<EventResponse> getUserEvents(Long userId, Pageable pageable) {
        // TODO: implement
        return null;
    }

    @Override
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        // TODO: implement
        return null;
    }
}
