package com.armycommunity.service.event;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.post.EventResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.exception.UnauthorizedException;
import com.armycommunity.mapper.EventMapper;
import com.armycommunity.model.post.Event;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.post.EventRepository;
import com.armycommunity.repository.user.UserRepository;
import com.armycommunity.service.activitylog.ActivityLogService;
import com.armycommunity.service.notification.NotificationService;
import com.armycommunity.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EventService interface for managing BTS/ARMY-related events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public EventResponse createEvent(Long userId, EventRequest request) {
        log.info("Creating event with title {} for userId: {}", request.getTitle(), userId);

        User creator = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        ;

        Event event = eventMapper.toEntity(request);
        event.setCreatedBy(creator);

        // Auto-verify events created by verified users or higher
        if (creator.canCreateVerifiedEvents()) {
            event.setIsVerified(true);
            event.setVerifiedBy(creator);
            event.setVerifiedAt(LocalDateTime.now());
            log.debug("Auto-verifying event created by user with role: {}", creator.getUserRole());
        }

        Event savedEvent = eventRepository.save(event);

        activityLogService.logActivity(
                userId,
                "CREATE_EVENT",
                "EVENT",
                savedEvent.getId(),
                Collections.emptyMap());

        log.info("Successfully created event with id: {} and title: {}", savedEvent.getId(), savedEvent.getTitle());
        return eventMapper.toResponse(savedEvent);


    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        log.info("Fetching event with id: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        log.info("Updating event with id: {} by userId: {}", eventId, userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user can update this event
        if (!canUserModifyEvent(user, event)) {
            log.warn("User {} attempted to update event {} without permission", userId, eventId);
            throw new UnauthorizedException("You don't have permission to update this event");
        }

        eventMapper.updateEventFromRequest(request, event);

        // If event was modified by someone other than the creator, mark as unverified
        if (!event.getCreatedBy().getId().equals(userId) && event.getIsVerified()) {
            event.setIsVerified(false);
            event.setVerifiedBy(null);
            event.setVerifiedAt(null);
            log.debug("Event {} unverified due to modification by different user", eventId);
        }

        Event updatedEvent = eventRepository.save(event);

        activityLogService.logActivity(
                userId,
                "UPDATE_EVENT",
                "EVENT",
                eventId,
                Collections.emptyMap());

        log.info("Successfully updated event with id: {}", eventId);
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        log.debug("Deleting event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user can delete this event
        if (!canUserModifyEvent(user, event)) {
            throw new UnauthorizedException("You don't have permission to delete this event");
        }

        eventRepository.delete(event);

        // Log activity
        activityLogService.logActivity(
                userId,
                "DELETE_EVENT",
                "EVENT",
                eventId,
                Collections.emptyMap());

        log.info("Successfully deleted event with id: {}", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents(int limit) {
        log.info("Fetching upcoming events with limit: {}", limit);

        LocalDateTime now = LocalDateTime.now();

        List<Event> events = eventRepository.findByEventDateAfterOrderByEventDateAsc(now)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching events between {} and {}", startDate, endDate);

        List<Event> events = eventRepository.findByEventDateBetween(startDate, endDate);
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByType(String eventType) {
        log.debug("Fetching events of type: {}", eventType);

        List<Event> events = eventRepository.findByEventType(eventType);
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getVerifiedEvents() {
        log.debug("Fetching verified events");

        List<Event> events = eventRepository.findByIsVerifiedTrueOrderByEventDateAsc();
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPendingVerificationEvents() {
        log.debug("Fetching events pending verification");

        List<Event> events = eventRepository.findByIsVerifiedFalse();
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUserEvents(Long userId) {
        log.debug("Fetching events created by user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Event> events = eventRepository.findByCreatedBy(userId);
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        log.debug("Fetching all events");

        List<Event> events = eventRepository.findAll(Sort.by("eventDate").descending());
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional
    public EventResponse verifyEvent(Long eventId, Long verifierId) {
        log.debug("Verifying event with id: {} by user: {}", eventId, verifierId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User verifier = userService.findById(verifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + verifierId));

        // Check if user can verify events
        if (!verifier.canModerate()) {
            throw new UnauthorizedException("You don't have permission to verify events");
        }

        if (event.getIsVerified()) {
            throw new IllegalStateException("Event is already verified");
        }

        event.setIsVerified(true);
        event.setVerifiedBy(verifier);
        event.setVerifiedAt(LocalDateTime.now());

        Event verifiedEvent = eventRepository.save(event);

        // Notify event creator if different from verifier
        if (!event.getCreatedBy().getId().equals(verifierId)) {
            NotificationRequest replyNotificationRequest = NotificationRequest.builder()
                    .userId(event.getCreatedBy().getId())
                    .type("EVENT_VERIFIED")
                    .message("Your event " + verifiedEvent.getTitle() + " has been verified")
                    .relatedEntityId(eventId)
                    .relatedEntityType("EVENT")
                    .build();

            notificationService.createNotification(replyNotificationRequest);
        }

        // Log activity
        activityLogService.logActivity(
                verifierId,
                "VERIFY_EVENT",
                "EVENT",
                eventId,
                Collections.emptyMap());

        log.info("Successfully verified event with id: {}", eventId);
        return eventMapper.toResponse(verifiedEvent);


    }

    @Override
    @Transactional(readOnly = true)
    public Long getPendingVerificationCount() {
        log.debug("Fetching pending verification count");
        return eventRepository.countByIsVerifiedFalse();
    }

    private Boolean canUserModifyEvent(User user, Event event) {
        // Event creator can always modify their events
        if (event.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }

        // Moderators and above can modify any event
        return user.canModerate();
    }
}
