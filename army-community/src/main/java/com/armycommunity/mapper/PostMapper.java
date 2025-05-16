package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.post.PostTag;
import com.armycommunity.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class})
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "imagePath", ignore = true)
    Post toEntity(PostRequest request, User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    void updateEntity(PostRequest request, @MappingTarget Post post);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userProfileImage", source = "user.profileImagePath")
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "isLikedByCurrentUser", ignore = true)
    @Mapping(target = "tags", ignore = true)
    PostResponse toResponse(Post post);
}
