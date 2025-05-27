package com.armycommunity.mapper;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.dto.response.post.TagResponse;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.model.post.Comment;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.post.PostTag;
import com.armycommunity.model.post.Reaction;
import com.armycommunity.model.user.User;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "postTags", ignore = true)
    @Mapping(target = "originalPost", ignore = true)
    @Mapping(target = "reposts", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "imagePath", ignore = true)
    Post toEntity(PostRequest request);

    @Mapping(target = "author", source = "user", qualifiedByName = "userToUserSummaryResponse")
    @Mapping(target = "tags", source = "postTags", qualifiedByName = "postTagsToTagResponses")
    @Mapping(target = "imagePaths", source = "imagePath", qualifiedByName = "imagePathToImagePaths")
    @Mapping(target = "likeCount", source = "reactions", qualifiedByName = "countLikes")
    @Mapping(target = "commentCount", source = "comments", qualifiedByName = "countActiveComments")
    @Mapping(target = "repostCount", source = "reposts", qualifiedByName = "countReposts")
    @Mapping(target = "isRepost", expression = "java(post.getOriginalPost() != null)")
    @Mapping(target = "originalPost", source = "originalPost", qualifiedByName = "postToSimplePostResponse")
    @Mapping(target = "isLikedByUser", ignore = true)
    @Mapping(target = "isRepostedByUser", ignore = true)
    @Mapping(target = "needsModeration", ignore = true)
    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "postTags", ignore = true)
    @Mapping(target = "originalPost", ignore = true)
    @Mapping(target = "reposts", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    void updatePostFromRequest(PostRequest request, @MappingTarget Post post);

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

    @Named("postTagsToTagResponses")
    default List<TagResponse> postTagsToTagResponses(Set<PostTag> postTags) {
        if (postTags == null || postTags.isEmpty()) {
            return List.of();
        }
        return postTags.stream()
                .map(postTag -> TagResponse.builder()
                        .id(postTag.getTag().getId())
                        .name(postTag.getTag().getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("imagePathToImagePaths")
    default List<String> imagePathToImagePaths(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return List.of();
        }
        // Handle multiple image paths separated by semicolon
        return List.of(imagePath.split(";"));
    }

    @Named("countLikes")
    default Long countLikes(Set<Reaction> reactions) {
        if (reactions == null) {
            return 0L;
        }
        return reactions.stream()
                .filter(reaction -> "LIKE".equals(reaction.getReactionType()))
                .count();
    }

    @Named("countActiveComments")
    default Long countActiveComments(Set<Comment> comments) {
        if (comments == null) {
            return 0L;
        }
        return comments.stream()
                .filter(comment -> !comment.getIsDeleted())
                .count();
    }

    @Named("countReposts")
    default Long countReposts(Set<Post> reposts) {
        if (reposts == null) {
            return 0L;
        }
        return reposts.stream()
                .filter(repost -> !repost.getIsDeleted())
                .count();
    }

    @Named("postToSimplePostResponse")
    default PostResponse postToSimplePostResponse(Post originalPost) {
        if (originalPost == null) {
            return null;
        }
        return PostResponse.builder()
                .id(originalPost.getId())
                .content(originalPost.getContent())
                .imagePaths(imagePathToImagePaths(originalPost.getImagePath()))
                .createdAt(originalPost.getCreatedAt())
                .author(userToUserSummaryResponse(originalPost.getUser()))
                .tags(postTagsToTagResponses(originalPost.getPostTags()))
                .likeCount(countLikes(originalPost.getReactions()))
                .commentCount(countActiveComments(originalPost.getComments()))
                .repostCount(countReposts(originalPost.getReposts()))
                .isRepost(false)
                .isDeleted(originalPost.getIsDeleted())
                .build();
    }
}
