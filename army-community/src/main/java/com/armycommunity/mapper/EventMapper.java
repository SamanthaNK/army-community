package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.EventRequest;
import com.armycommunity.dto.response.post.EventResponse;
import com.armycommunity.model.post.Event;
import com.armycommunity.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventRequest request, User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(EventRequest request, @MappingTarget Event event);

    @Mapping(target = "creatorId", source = "createdBy.id")
    @Mapping(target = "creatorUsername", source = "createdBy.username")
    EventResponse toResponse(Event event);
}
