package com.armycommunity.service.post;

import com.armycommunity.dto.request.post.PostRequest;
import com.armycommunity.dto.response.post.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponse createPost(Long userId, PostRequest request, MultipartFile image);

    PostResponse getPost(Long postId);

    PostResponse updatePost(Long postId, Long userId, PostRequest request);

    void deletePost(Long postId, Long userId);

    Page<PostResponse> getUserPosts(Long userId, Pageable pageable);

    Page<PostResponse> getFeedPosts(Long userId, Pageable pageable);

    Page<PostResponse> getAllPosts(Pageable pageable);

    Page<PostResponse> searchPosts(String query, Pageable pageable);

    Page<PostResponse> getPostsByTag(String tagName, Pageable pageable);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    List<PostResponse> getTrendingPosts(int limit);
}
