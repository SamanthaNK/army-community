package com.armycommunity.mapper;

import com.armycommunity.dto.response.user.NotificationResponse;
import com.armycommunity.model.user.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
