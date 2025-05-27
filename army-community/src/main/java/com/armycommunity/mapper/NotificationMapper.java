package com.armycommunity.mapper;

import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.user.NotificationResponse;
import com.armycommunity.model.user.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    Notification toEntity(NotificationRequest notificationRequest);

    @Mapping(target = "username", source = "user.username")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
