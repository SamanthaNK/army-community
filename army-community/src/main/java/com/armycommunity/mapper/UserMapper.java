package com.armycommunity.mapper;

import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserDetailResponse;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileImagePath", ignore = true)
    @Mapping(target = "userRole", constant = "USER")
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verificationType", ignore = true)
    @Mapping(target = "oauthProvider", ignore = true)
    @Mapping(target = "oauthId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "password", ignore = true)
        // Password handled separately
    User toEntity(UserRegistrationRequest request);

    @Mapping(target = "isVerified", expression = "java(user.isVerified())")
    @Mapping(target = "canModerate", expression = "java(user.canModerate())")
    @Mapping(target = "isSuspended", expression = "java(user.isSuspended())")
    @Mapping(target = "postCount", expression = "java(user.getPosts().size())")
    @Mapping(target = "followerCount", expression = "java(user.getFollowers().size())")
    @Mapping(target = "followingCount", expression = "java(user.getFollowing().size())")
    @Mapping(target = "collectionsCount", expression = "java(user.getCollections().size())")
    @Mapping(target = "isFollowing", ignore = true)
    @Mapping(target = "isOwnProfile", ignore = true)
    UserDetailResponse toDetailResponse(User user);

    @Mapping(target = "isVerified", expression = "java(user.isVerified())")
    @Mapping(target = "isSuspended", expression = "java(user.isSuspended())")
    UserSummaryResponse toSummaryResponse(User user);

    List<UserSummaryResponse> toSummaryResponseList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true) // Username cannot be updated
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileImagePath", ignore = true)
    @Mapping(target = "userRole", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verificationType", ignore = true)
    @Mapping(target = "oauthProvider", ignore = true)
    @Mapping(target = "oauthId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "suspendedUntil", ignore = true)
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);
}
