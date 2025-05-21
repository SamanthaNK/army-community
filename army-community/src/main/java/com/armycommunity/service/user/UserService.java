package com.armycommunity.service.user;


import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserProfileResponse;
import com.armycommunity.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(UserRegistrationRequest request);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse getUserProfileByUsername(String username);

    User updateUser(Long userId, UserUpdateRequest request);

    User updateUserProfileImage(Long userId, MultipartFile imageFile) throws IOException;

    void deleteUser(Long userId);

    Page<UserProfileResponse> searchUsers(String keyword, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User saveUser(User user);

    List<UserProfileResponse> getRecentlyActiveUsers(int limit);
}
