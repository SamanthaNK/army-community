package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.response.post.CommentResponse;
import com.armycommunity.model.post.Comment;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "post", source = "post")
    @Mapping(target = "parentCommentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    Comment toEntity(CommentRequest request, User user, Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parentCommentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(CommentRequest request, @MappingTarget Comment comment);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userProfileImage", source = "user.profileImagePath")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "replies", ignore = true)
    CommentResponse toResponse(Comment comment);
}
