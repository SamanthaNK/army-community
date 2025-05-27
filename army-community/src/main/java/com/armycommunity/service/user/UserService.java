package com.armycommunity.service.user;


import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserDetailResponse;
import com.armycommunity.dto.response.user.UserRoleStatistics;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.model.user.User;
import com.armycommunity.model.user.UserRole;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDetailResponse registerUser(UserRegistrationRequest request);

    UserDetailResponse getUserProfile(Long userId);

    UserDetailResponse getUserProfileByUsername(String username, Long currentUserId);

    UserDetailResponse updateUser(Long userId, UserUpdateRequest request);

    String updateUserProfileImage(Long userId, MultipartFile imageFile);

    void deleteUser(Long userId);

    List<UserSummaryResponse> searchUsers(String query);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    List<UserSummaryResponse> getUsersByRole(UserRole role);

    List<UserSummaryResponse> getVerifiedUsers();

    User saveUser(User user);

    List<UserSummaryResponse> getRecentlyActiveUsers(int limit);

    void promoteUser(Long userId, UserRole newRole, Long promotedBy);

    void verifyUser(Long userId, String verificationType, Long verifiedBy);

    void demoteUser(Long userId, UserRole newRole, String reason, Long demotedBy);

    void suspendUser(Long userId, String reason, LocalDateTime suspendUntil, Long suspendedBy);

    void unsuspendUser(Long userId, Long unsuspendedBy);

    List<UserSummaryResponse> getModerators();

    List<UserSummaryResponse> getSuspendedUsers();

    List<UserSummaryResponse> getUsersNeedingModeration();

    UserRoleStatistics getRoleStatistics();

    void processExpiredSuspensions();
}
