package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.model.post.Event;
import com.armycommunity.model.user.User;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper interface for Event entity transformations.
 * Handles mapping between Event entities and their corresponding DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "isVerified", constant = "false")
    Event toEntity(EventRequest request);

    @Mapping(target = "creatorId", source = "createdBy.id")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToUserSummaryResponse")
    @Mapping(target = "verifiedBy", source = "verifiedBy", qualifiedByName = "userToUserSummaryResponse")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verifier", ignore = true)
    void updateEventFromRequest(EventRequest request, @MappingTarget Event event);

    @Named("userToUserSummaryResponse")
    default UserSummaryResponse userToUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImagePath(user.getProfileImagePath())
                .userRole(user.getUserRole())
                .isVerified(user.isVerified())
                .build();
    }
}
