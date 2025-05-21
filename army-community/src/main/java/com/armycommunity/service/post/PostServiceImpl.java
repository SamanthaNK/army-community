package com.armycommunity.service.post;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.PostMapper;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.post.PostTag;
import com.armycommunity.model.post.Reaction;
import com.armycommunity.model.post.Tag;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.post.*;
import com.armycommunity.repository.user.UserRepository;
import com.armycommunity.service.activitylog.ActivityLogService;
import com.armycommunity.service.filestorage.FileStorageService;
import com.armycommunity.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PostResponse createPost(Long userId, PostRequest request, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Post post = postMapper.toEntity(request, user);

        // Handle image if present
        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = fileStorageService.storeFile(image, "posts");
                post.setImagePath(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store post image", e);
            }
        }

        Post savedPost = postRepository.save(post);

        // Extract and save tags
        Set<Tag> tags = extractAndSaveTags(request.getContent(), savedPost);

        // Log activity
        activityLogService.logActivity(userId, "POST_CREATE", "POST", savedPost.getId(), Collections.emptyMap());

        // Use buildPostResponse to include all necessary fields
        return buildPostResponse(savedPost, userId);
    }

    @Override
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        return buildPostResponse(post, null);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this post");
        }

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        postMapper.updateEntity(request, post);
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        // Extract and save tags - use the same method as in createPost
        Set<Tag> tags = extractAndSaveTags(request.getContent(), updatedPost);

        // Log activity
        activityLogService.logActivity(userId, "POST_UPDATE", "POST", updatedPost.getId(), Collections.emptyMap());

        return buildPostResponse(updatedPost, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this post");
        }

        post.setIsDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        // Log activity
        activityLogService.logActivity(userId, "POST_DELETE", "POST", postId, Collections.emptyMap());
    }

    @Override
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return posts.map(post -> buildPostResponse(post, userId));
    }

    @Override
    public Page<PostResponse> getFeedPosts(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<Post> posts = postRepository.findPostsFromFollowedUsers(userId, pageable);
        return posts.map(post -> buildPostResponse(post, userId));
    }

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllActivePosts(pageable);
        return posts.map(post -> buildPostResponse(post, null));
    }

    @Override
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPosts(pageable);
        }

        Page<Post> posts = postRepository.searchPosts(query.trim(), pageable);
        return posts.map(post -> buildPostResponse(post, null));
    }

    @Override
    public Page<PostResponse> getPostsByTag(String tagName, Pageable pageable) {
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with name: " + tagName));

        Page<Post> posts = postRepository.findPostsByTagId(tag.getId(), pageable);
        return posts.map(post -> buildPostResponse(post, null));
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        if (!reactionRepository.existsByUserIdAndPostId(userId, postId)) {
            Reaction reaction = new Reaction();
            reaction.setUser(user);
            reaction.setPost(post);
            reaction.setReactionType("LIKE");
            reactionRepository.save(reaction);

            // Log activity
            activityLogService.logActivity(userId, "POST_LIKE", "POST", postId, Collections.emptyMap());

            // Send notification if the post is not by the same user
            if (!post.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                        post.getUser().getId(),
                        "POST_LIKE",
                        user.getUsername() + " liked your post",
                        postId,
                        "POST"
                );
            }
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        reactionRepository.deleteByUserIdAndPostId(userId, postId);

        // Log activity
        activityLogService.logActivity(userId, "POST_UNLIKE", "POST", postId, Collections.emptyMap());
    }

    @Override
    public List<PostResponse> getTrendingPosts(int limit) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Post> trendingPosts = postRepository.findTrendingPosts(oneWeekAgo, limit);
        return trendingPosts.stream()
                .map(post -> buildPostResponse(post, null))
                .collect(Collectors.toList());
    }

    // Helper method to extract hashtags from post content and save them
    private Set<Tag> extractAndSaveTags(String content, Post post) {
        Set<Tag> tags = new HashSet<>();

        // Extract hashtags using regex
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);

        List<String> tagNames = new ArrayList<>();
        while (matcher.find()) {
            tagNames.add(matcher.group(1).toLowerCase());
        }

        // Save unique tags
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        newTag.setCreatedAt(LocalDateTime.now());
                        return tagRepository.save(newTag);
                    });

            tags.add(tag);

            // Create post-tag association
            PostTag postTag = new PostTag();
            postTag.setPost(post);
            postTag.setTag(tag);
            postTagRepository.save(postTag);
        }

        return tags;
    }

    private PostResponse buildPostResponse(Post post, Long currentUserId) {
        PostResponse response = postMapper.toResponse(post);

        // Set comment count
        long commentCount = commentRepository.countByPostId(post.getId());
        response.setCommentCount(commentCount);

        // Set like count
        long likeCount = reactionRepository.countByPostId(post.getId());
        response.setLikeCount(likeCount);

        // Set whether the current user has liked the post
        if (currentUserId != null) {
            boolean isLiked = reactionRepository.existsByUserIdAndPostId(currentUserId, post.getId());
            response.setLikedByCurrentUser(isLiked);
        }

        // Set tags
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            List<String> tagNames = post.getTags().stream()
                    .map(PostTag::getTag)    // First get Tag from PostTag
                    .map(Tag::getName)        // Then get name from Tag
                    .collect(Collectors.toList());
            response.setTags(tagNames);
        } else {
            response.setTags(Collections.emptyList());
        }

        return response;
    }
}
