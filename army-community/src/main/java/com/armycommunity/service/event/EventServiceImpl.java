package com.armycommunity.service.event;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.exception.UnauthorizedException;
import com.armycommunity.mapper.EventMapper;
import com.armycommunity.model.post.Event;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.post.EventRepository;
import com.armycommunity.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse createEvent(Long userId, EventRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Event event = eventMapper.toEntity(request, user);
        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        // Check if the user is the creator of the event or has admin role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!event.getCreatedBy().getId().equals(userId) && !user.getRole().equals("ADMIN")) {
            throw new UnauthorizedException("You don't have permission to update this event");
        }

        eventMapper.updateEntity(request, event);
        Event updatedEvent = eventRepository.save(event);

        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Check if the user is the creator of the event or has admin role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!event.getCreatedBy().getId().equals(userId) && !user.getRole().equals("ADMIN")) {
            throw new UnauthorizedException("You don't have permission to delete this event");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents(int limit) {
        LocalDateTime now = LocalDateTime.now();

        return eventRepository.findByEventDateAfterOrderByEventDateAsc(now)
                .stream()
                .limit(limit)
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return eventRepository.findByEventDateBetween(startDateTime, endDateTime)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByType(String eventType) {
        return eventRepository.findByEventType(eventType)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getUserEvents(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return eventRepository.findByCreatedBy(userId, pageable)
                .map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(eventMapper::toResponse);
    }
}
