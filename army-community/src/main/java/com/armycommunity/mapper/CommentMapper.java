package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.response.post.CommentResponse;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.model.post.Comment;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.user.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "parentCommentId", ignore = true)
    Comment toEntity(CommentRequest request, User user, Post post);

    @Mapping(target = "user", source = "user", qualifiedByName = "userToUserSummaryResponse")
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "replies", source = "replies", qualifiedByName = "commentSetToCommentResponseList")
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "parentCommentId", ignore = true)
    void updateCommentFromRequest(CommentRequest request, @MappingTarget Comment comment);

    @Named("userToUserSummaryResponse")
    default UserSummaryResponse userToUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImagePath(user.getProfileImagePath())
                .build();
    }

    @Named("commentSetToCommentResponseList")
    default List<CommentResponse> commentSetToCommentResponseList(Set<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
