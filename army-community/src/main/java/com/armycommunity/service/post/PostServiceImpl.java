package com.armycommunity.service.post;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.exception.UnauthorizedException;
import com.armycommunity.exception.ValidationException;
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
import com.armycommunity.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final ReactionRepository reactionRepository;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final UserService userService;

    private static final int MAX_IMAGES_PER_POST = 4;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Override
    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        log.info("Creating new post for user ID: {}", userId);

        validatePostRequest(request);
        User user = getUserById(userId);

        Post post = postMapper.toEntity(request);
        post.setUser(user);

        // Handle repost
        if (request.isRepost()) {
            handleRepost(post, request, user);
        }

        // Handle image uploads
        if (request.hasImages()) {
            String imagePaths = handleImageUploads(request.getImages(), userId);
            post.setImagePath(imagePaths);
            log.debug("Uploaded {} images for post", request.getImages().size());
        }

        // Save post first to get ID
        Post savedPost = postRepository.save(post);
        log.debug("Saved post with ID: {}", savedPost.getId());

        // Handle tags
        if (request.hasTags()) {
            handlePostTags(savedPost, request.getTags());
        }

        // Log activity
        Map<String, Object> activityDetails = new HashMap<>();
        activityDetails.put("action", "Created post" + (request.isRepost() ? " (repost)" : ""));
        activityDetails.put("postContent", truncateString(savedPost.getContent(), 100));
        if (request.isRepost()) {
            activityDetails.put("originalPostId", request.getOriginalPostId());
        }
        activityLogService.logActivity(userId, "CREATE_POST", "POST", savedPost.getId(), activityDetails);

        // Send notifications for reposts
        if (request.isRepost() && savedPost.getOriginalPost() != null) {
            sendRepostNotification(savedPost);
        }

        PostResponse response = postMapper.toResponse(savedPost);
        enrichPostResponse(response, userId);

        log.info("Successfully created post ID: {} for user ID: {}", savedPost.getId(), userId);
        return response;
    }

    @Override
    @Transactional
    public PostResponse getPost(Long postId, Long currentUserId) {
        log.debug("Fetching post ID: {} for user ID: {}", postId, currentUserId);

        Post post = getPostById(postId);

        if (post.getIsDeleted() && !canViewDeletedPost(post, currentUserId)) {
            throw new ResourceNotFoundException("Post not found or has been deleted");
        }

        PostResponse response = postMapper.toResponse(post);
        enrichPostResponse(response, currentUserId);

        return response;
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostRequest request) {
        log.info("Updating post ID: {} by user ID: {}", postId, userId);

        validatePostRequest(request);
        Post post = getPostById(postId);

        if (!canEditPost(post, userId)) {
            throw new UnauthorizedException("You can only edit your own posts");
        }

        if (request.isRepost()) {
            throw new ValidationException("Cannot update a post to be a repost");
        }

        String oldContent = post.getContent();
        String oldImagePath = post.getImagePath();

        postMapper.updatePostFromRequest(request, post);

        // Handle new image uploads
        if (request.hasImages()) {
            // Delete old images
            if (oldImagePath != null) {
                deletePostImages(oldImagePath);
            }
            String newImagePaths = handleImageUploads(request.getImages(), userId);
            post.setImagePath(newImagePaths);
            log.debug("Updated images for post ID: {}", postId);
        }

        // Update tags
        if (request.hasTags()) {
            // Remove existing tags
            postTagRepository.deleteByPostId(postId);
            handlePostTags(post, request.getTags());
        }

        Post updatedPost = postRepository.save(post);

        // Log activity
        Map<String, Object> activityDetails = new HashMap<>();
        activityDetails.put("action", "Updated post");
        activityDetails.put("oldContent", truncateString(oldContent, 50));
        activityDetails.put("newContent", truncateString(updatedPost.getContent(), 50));
        activityDetails.put("imagesChanged", request.hasImages());
        activityLogService.logActivity(userId, "UPDATE_POST", "POST", postId, activityDetails);

        PostResponse response = postMapper.toResponse(updatedPost);
        enrichPostResponse(response, userId);

        log.info("Successfully updated post ID: {}", postId);
        return response;
    }

    public void moderatePost(Long postId, Long moderatorId, String action, String reason) {
        User moderator = userService.findById(moderatorId).orElseThrow(() -> new IllegalArgumentException("Moderator not found"));

        if (!moderator.canModerate()) {
            throw new SecurityException("Insufficient permissions to moderate posts");
        }

        Post post = getPostById(postId);

        switch (action.toUpperCase()) {
            case "DELETE":
                post.setIsDeleted(true);
                break;
            case "HIDE":
                // TODO: Implement hiding logic if needed
                break;
            case "APPROVE":
                // TODO: Implement approval logic if needed
                break;
            default:
                throw new ValidationException("Invalid moderation action: " + action);
        }

        postRepository.save(post);

        activityLogService.logActivity(
                moderatorId,
                "POST_MODERATION",
                "Post", postId,
                Collections.emptyMap());

        // Notify post author
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(post.getUser().getId())
                .type("POST_MODERATED")
                .message("Your post has been moderated. Action: " + action + ". Reason: " + reason)
                .relatedEntityId(postId)
                .relatedEntityType("POST")
                .build();
        notificationService.createNotification(notificationRequest);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        log.info("Deleting post ID: {} by user ID: {}", postId, userId);

        Post post = getPostById(postId);

        if (!canDeletePost(post, userId)) {
            throw new UnauthorizedException("You can only delete your own posts or must be a moderator");
        }

        // Soft delete
        post.setIsDeleted(true);
        postRepository.save(post);

        // Delete associated images
        if (post.getImagePath() != null) {
            deletePostImages(post.getImagePath());
        }

        // Log activity
        Map<String, Object> activityDetails = new HashMap<>();
        activityDetails.put("action", "Deleted post");
        activityDetails.put("postContent", truncateString(post.getContent(), 100));
        activityDetails.put("hadImages", post.getImagePath() != null);
        activityLogService.logActivity(userId, "DELETE_POST", "POST", postId, activityDetails);

        log.info("Successfully deleted post ID: {}", postId);
    }

    @Override
    @Transactional
    public PostResponse likePost(Long postId, Long userId) {
        log.debug("User ID: {} liking post ID: {}", userId, postId);

        Post post = getPostById(postId);
        User user = getUserById(userId);

        if (reactionRepository.existsByUserIdAndPostId(userId, postId)) {
            log.debug("User ID: {} already liked post ID: {}", userId, postId);
            throw new ValidationException("You have already liked this post");
        }

        Reaction reaction = Reaction.builder()
                .user(user)
                .post(post)
                .reactionType("LIKE")
                .createdAt(LocalDateTime.now())
                .build();

        reactionRepository.save(reaction);

        // Send notification to post author (if not self-like)
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createLikeNotification(
                    post.getUser().getId(),
                    userId,
                    postId,
                    user.getUsername()
            );
        }

        /// Log activity
        Map<String, Object> activityDetails = new HashMap<>();
        activityDetails.put("action", "Liked post");
        activityDetails.put("postAuthor", post.getUser().getUsername());
        activityLogService.logActivity(userId, "LIKE_POST", "POST", postId, activityDetails);


        PostResponse response = postMapper.toResponse(post);
        enrichPostResponse(response, userId);

        log.debug("User ID: {} successfully liked post ID: {}", userId, postId);
        return response;
    }

    @Override
    @Transactional
    public PostResponse unlikePost(Long postId, Long userId) {
        log.debug("User ID: {} unliking post ID: {}", userId, postId);

        Post post = getPostById(postId);

        if (!reactionRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ValidationException("You have not liked this post");
        }

        reactionRepository.deleteByUserIdAndPostId(userId, postId);

        // Log activity
        Map<String, Object> activityDetails = new HashMap<>();
        activityDetails.put("action", "Unliked post");
        activityDetails.put("postAuthor", post.getUser().getUsername());
        activityLogService.logActivity(userId, "UNLIKE_POST", "POST", postId, activityDetails);

        PostResponse response = postMapper.toResponse(post);
        enrichPostResponse(response, userId);

        log.debug("User ID: {} successfully unliked post ID: {}", userId, postId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getFeedPosts(Long userId, int page, int size) {
        log.debug("Fetching feed posts for user ID: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findPostsFromFollowedUsers(userId, pageable);

        List<PostResponse> responses = postMapper.toResponseList(posts.getContent());
        responses.forEach(response -> enrichPostResponse(response, userId));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getUserPosts(Long userId, int page, int size) {
        log.debug("Fetching posts for user ID: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<PostResponse> responses = postMapper.toResponseList(posts.getContent());
        responses.forEach(response -> enrichPostResponse(response, userId));
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getTrendingPosts(int page, int size) {
        log.debug("Fetching trending posts, page: {}, size: {}", page, size);

        LocalDateTime since = LocalDateTime.now().minusDays(7); // Get trending posts from last 7 days
        List<Post> posts = postRepository.findTrendingPosts(since, size);

        return postMapper.toResponseList(posts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String query, int page, int size) {
        log.debug("Searching posts with query: '{}', page: {}, size: {}", query, page, size);

        if (query == null || query.trim().length() < 2) {
            throw new ValidationException("Search query must be at least 2 characters long");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.searchPosts(query.trim(), pageable);

        return postMapper.toResponseList(posts.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByTag(String tagName, int page, int size) {
        log.debug("Fetching posts by tag: '{}', page: {}, size: {}", tagName, page, size);

        if (tagName == null || tagName.trim().isEmpty()) {
            throw new ValidationException("Tag name cannot be empty");
        }

        String normalizedTagName = tagName.trim().toLowerCase();

        // Find the tag first
        Tag tag = tagRepository.findByName(normalizedTagName)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + tagName));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findPostsByTagId(tag.getId(), pageable);

        return postMapper.toResponseList(posts.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(int page, int size) {
        log.debug("Fetching all posts, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllActivePosts(pageable);

        List<PostResponse> responses = postMapper.toResponseList(posts.getContent());

        log.debug("Retrieved {} posts", responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getPostsForModeration() {
        log.debug("Fetching posts that need moderation");

        // Get posts with sensitive content keywords or from regular users
        List<Post> posts = postRepository.findPostsNeedingModeration("sensitive");

        log.debug("Found {} posts requiring moderation", posts.size());
        return posts;
    }


    // Helper methods

    private void validatePostRequest(PostRequest request) {
        if (request.hasImages() && request.getImages().size() > MAX_IMAGES_PER_POST) {
            throw new ValidationException("Cannot upload more than " + MAX_IMAGES_PER_POST + " images");
        }

        if (request.hasImages()) {
            for (MultipartFile image : request.getImages()) {
                if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
                    throw new ValidationException("Unsupported image type: " + image.getContentType());
                }
                if (image.getSize() > 10 * 1024 * 1024) { // 10MB limit
                    throw new ValidationException("Image size cannot exceed 10MB");
                }
            }
        }
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private void handleRepost(Post post, PostRequest request, User user) {
        Post originalPost = getPostById(request.getOriginalPostId());

        if (originalPost.getIsDeleted()) {
            throw new ValidationException("Cannot repost a deleted post");
        }

        if (originalPost.getUser().getId().equals(user.getId())) {
            throw new ValidationException("Cannot repost your own post");
        }

        post.setOriginalPost(originalPost);
        if (request.hasRepostComment()) {
            post.setRepostComment(request.getRepostComment());
        }

        log.debug("Setting up repost of post ID: {} by user ID: {}", request.getOriginalPostId(), user.getId());
    }

    private String handleImageUploads(List<MultipartFile> images, Long userId) {
        try {
            List<String> imagePaths = new ArrayList<>();

            for (MultipartFile image : images) {
                String fileName = String.format("posts/%d/%d_%s",
                        userId, System.currentTimeMillis(), image.getOriginalFilename());
                String savedPath = fileStorageService.storeFile(image, fileName);
                imagePaths.add(savedPath);
            }

            return String.join(";", imagePaths);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload images: " + e.getMessage());
        }
    }

    private void handlePostTags(Post post, List<String> tagNames) {
        Set<PostTag> postTags = new HashSet<>();

        for (String tagName : tagNames) {
            String normalizedTagName = tagName.trim().toLowerCase();
            if (!normalizedTagName.isEmpty()) {
                Tag tag = tagRepository.findByName(normalizedTagName)
                        .orElseGet(() -> {
                            Tag newTag = Tag.builder()
                                    .name(normalizedTagName)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            return tagRepository.save(newTag);
                        });

                PostTag postTag = PostTag.builder()
                        .post(post)
                        .tag(tag)
                        .build();
                postTags.add(postTag);
            }
        }

        post.setPostTags(postTags);
        log.debug("Added {} tags to post ID: {}", postTags.size(), post.getId());
    }

    private void deletePostImages(String imagePath) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            String[] paths = imagePath.split(";");
            for (String path : paths) {
                try {
                    fileStorageService.deleteFile(path);
                } catch (Exception e) {
                    log.warn("Failed to delete image: {}", path, e);
                }
            }
        }
    }

    private void sendRepostNotification(Post repost) {
        if (repost.getOriginalPost() != null && repost.getOriginalPost().getUser() != null) {
            notificationService.createRepostNotification(
                    repost.getOriginalPost().getUser().getId(),
                    repost.getUser().getId(),
                    repost.getOriginalPost().getId(),
                    repost.getUser().getUsername()
            );
        }
    }

    private void enrichPostResponse(PostResponse response, Long currentUserId) {
        if (currentUserId != null) {
            response.setIsLikedByUser(
                    reactionRepository.existsByUserIdAndPostId(currentUserId, response.getId())
            );

            // Check if user has reposted this post
            response.setIsRepostedByUser(
                    postRepository.existsByUserIdAndOriginalPostId(currentUserId, response.getId())
            );
        } else {
            response.setIsLikedByUser(false);
            response.setIsRepostedByUser(false);
        }

        // Set moderation flag (simplified logic)
        response.setNeedsModeration(false);
    }

    private boolean canViewDeletedPost(Post post, Long currentUserId) {
        if (currentUserId == null) return false;

        User currentUser = getUserById(currentUserId);
        return post.getUser().getId().equals(currentUserId) ||
                currentUser.canModerate();
    }

    private boolean canEditPost(Post post, Long userId) {
        return post.getUser().getId().equals(userId);
    }

    private boolean canDeletePost(Post post, Long userId) {
        User user = getUserById(userId);
        return post.getUser().getId().equals(userId) || user.canModerate();
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
