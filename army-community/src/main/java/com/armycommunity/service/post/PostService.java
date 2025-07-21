package com.armycommunity.service.post;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponse createPost(Long userId, PostRequest request);

    PostResponse getPost(Long postId, Long currentUserId);

    PostResponse updatePost(Long postId, Long userId, PostRequest request);

    void moderatePost(Long postId, Long moderatorId, String action, String reason);

    void deletePost(Long postId, Long userId);

    PostResponse likePost(Long postId, Long userId);

    PostResponse unlikePost(Long postId, Long userId);

    List<PostResponse> getFeedPosts(Long userId, int page, int size);

    List<PostResponse> getUserPosts(Long userId, int page, int size);

    List<PostResponse> getTrendingPosts(int page, int size);

    List<PostResponse> searchPosts(String query, int page, int size);

    List<PostResponse> getPostsByTag(String tagName, int page, int size);

    List<PostResponse> getAllPosts(int page, int size);

    List<Post> getPostsForModeration();
}
