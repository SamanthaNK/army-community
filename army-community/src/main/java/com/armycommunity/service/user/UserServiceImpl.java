package com.armycommunity.service.user;

import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserDetailResponse;
import com.armycommunity.dto.response.user.UserRoleStatistics;
import com.armycommunity.dto.response.user.UserSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.UserMapper;
import com.armycommunity.model.user.User;
import com.armycommunity.model.user.UserRole;
import com.armycommunity.repository.user.FollowRepository;
import com.armycommunity.repository.user.UserRepository;
import com.armycommunity.service.activitylog.ActivityLogService;
import com.armycommunity.service.filestorage.FileStorageService;
import com.armycommunity.service.notification.NotificationService;
import com.armycommunity.dto.request.user.NotificationRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public UserDetailResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {} and username: {}",
                savedUser.getId(), savedUser.getUsername());

        // Log activity
        activityLogService.logActivity(
                savedUser.getId(),
                "USER_REGISTERED",
                "User", savedUser.getId(),
                Collections.emptyMap());

        return userMapper.toDetailResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserProfile(Long userId) {
        log.info("Fetching user profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserProfileByUsername(String username, Long currentUserId) {
        log.info("Fetching user profile for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        UserDetailResponse response = userMapper.toDetailResponse(user);

        // Set context flags
        response.setOwnProfile(currentUserId != null && currentUserId.equals(user.getId()));

        if (currentUserId != null && !response.isOwnProfile()) {
            // Check if current user is following this profile
            response.setFollowing(isFollowing(currentUserId, user.getId()));
        } else {
            response.setFollowing(false);
        }

        // Hide sensitive information for non-own profiles and non-admin users
        if (!response.isOwnProfile()) {
            response.setEmail(null);
            response.setOauthProvider(null);
        }

        return response;
    }

    @Override
    @Transactional
    public UserDetailResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("Updating user profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        String originalEmail = user.getEmail();

        try {
            // Check if email is being changed and if new email already exists
            if (request.getEmail() != null && !request.getEmail().equals(originalEmail)) {
                if (existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }

            // Handle password change
            if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    throw new IllegalArgumentException("Current password is incorrect");
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                log.info("Password updated for user ID: {}", userId);
            }

            // Handle profile image upload
            if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
                String oldImagePath = user.getProfileImagePath();
                String newImagePath = updateUserProfileImage(userId, request.getProfileImage());
                user.setProfileImagePath(newImagePath);

                // Delete old image if it exists
                if (oldImagePath != null) {
                    try {
                        fileStorageService.deleteFile(oldImagePath);
                        log.debug("Deleted old profile image: {}", oldImagePath);
                    } catch (Exception e) {
                        log.warn("Failed to delete old profile image: {}", oldImagePath, e);
                    }
                }

                log.info("Profile image updated for user ID: {}", userId);
            }

            // Update other fields
            userMapper.updateUserFromRequest(request, user);

            User updatedUser = userRepository.save(user);
            log.info("User profile updated successfully for ID: {}", userId);

            // Log activity
            activityLogService.logActivity(userId, "PROFILE_UPDATED", "USER", userId, null);

            return userMapper.toDetailResponse(updatedUser);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    @Override
    @Transactional
    public String updateUserProfileImage(Long userId, MultipartFile imageFile) {
        log.info("Updating profile image for user ID: {}", userId);

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        // Validate file type
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (e.g., max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("Image file size must not exceed 5MB");
        }

        try {
            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() +
                    getFileExtension(imageFile.getOriginalFilename());
            String imagePath = fileStorageService.storeFile(imageFile, fileName);

            log.info("Profile image uploaded successfully for user ID: {}, path: {}", userId, imagePath);
            return imagePath;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile image", e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete profile image if exists
        if (user.getProfileImagePath() != null) {
            try {
                fileStorageService.deleteFile(user.getProfileImagePath());
                log.debug("Deleted profile image for user ID: {}", userId);
            } catch (Exception e) {
                log.warn("Failed to delete profile image for user ID: {}", userId, e);
            }
        }

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> searchUsers(String query) {
        log.debug("Searching users with query: {}", query);

        try {
            List<User> users = userRepository.searchUsers(query);
            log.debug("Found {} users for query: {}", users.size(), query);
            return userMapper.toSummaryResponseList(users);

        } catch (Exception e) {
            log.error("Error searching users with query: {}", query, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUsersByRole(UserRole role) {
        log.debug("Fetching users with role: {}", role);

        List<User> users = userRepository.findByUserRole(role);
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getVerifiedUsers() {
        log.debug("Fetching verified users");

        List<User> users = userRepository.findByVerifiedAtIsNotNull();
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void promoteUser(Long userId, UserRole newRole, Long promotedBy) {
        log.info("Promoting user ID: {} to role: {} by user ID: {}", userId, newRole, promotedBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        User promoter = findById(promotedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Promoter not found with id: " + promotedBy));

        // Only admins can promote users
        if (!promoter.hasMinimumRole(UserRole.ADMIN)) {
            throw new SecurityException("Insufficient permissions to promote user");
        }

        // Super admins can only be created by other super admins
        if (newRole == UserRole.SUPER_ADMIN && !promoter.hasRole(UserRole.SUPER_ADMIN)) {
            throw new SecurityException("Only super admins can create other super admins");
        }

        UserRole oldRole = user.getUserRole();
        user.setUserRole(newRole);

        if (newRole.hasPermissionLevel(UserRole.VERIFIED) && user.getVerifiedAt() == null) {
            user.setVerifiedAt(LocalDateTime.now());
            user.setVerificationType("MANUAL");
        }

        User savedUser = saveUser(user);

        // Log activity with details
        Map<String, Object> details = new HashMap<>();
        details.put("oldRole", oldRole.name());
        details.put("newRole", newRole.name());
        details.put("promotedUsername", user.getUsername());

        activityLogService.logActivity(
                promotedBy,
                "USER_PROMOTION",
                "User",
                userId,
                details);

        // Notify the promoted user
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type("USER_PROMOTED")
                .message("Your account has been promoted to " + newRole.getDescription())
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .build();

        notificationService.createNotification(notificationRequest);

        log.info("User ID: {} successfully promoted from {} to {}", userId, oldRole, newRole);
    }

    @Override
    @Transactional
    public void verifyUser(Long userId, String verificationType, Long verifiedBy) {
        log.info("Verifying user ID: {} with type: {} by user ID: {}", userId, verificationType, verifiedBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        User verifier = userRepository.findById(verifiedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found with id: " + verifiedBy));

        if (!verifier.canModerate()) {
            throw new SecurityException("Insufficient permissions to verify user");
        }

        UserRole oldRole = user.getUserRole();

        if (!user.hasMinimumRole(UserRole.VERIFIED)) {
            user.setUserRole(UserRole.VERIFIED);
        }
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerificationType(verificationType);

        User savedUser = saveUser(user);

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("verificationType", verificationType);
        details.put("verifiedUsername", user.getUsername());

        activityLogService.logActivity(
                verifiedBy,
                "USER_VERIFIED",
                "User",
                userId,
                details);

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type("USER_VERIFIED")
                .message("Congratulations! Your account has been verified.")
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .build();

        notificationService.createNotification(notificationRequest);

        log.info("User ID: {} successfully verified with type: {}", userId, verificationType);
    }

    @Override
    @Transactional
    public void demoteUser(Long userId, UserRole newRole, String reason, Long demotedBy) {
        log.info("Demoting user ID: {} to role: {} by user ID: {} for reason: {}",
                userId, newRole, demotedBy, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        User demoter = userRepository.findById(demotedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Demoter not found with id: " + demotedBy));

        // Only admins can demote users
        if (!demoter.hasMinimumRole(UserRole.ADMIN)) {
            throw new SecurityException("Insufficient permissions to demote user");
        }

        // Super admins can only be demoted by other super admins
        if (user.hasRole(UserRole.SUPER_ADMIN) && !demoter.hasRole(UserRole.SUPER_ADMIN)) {
            throw new SecurityException("Only super admins can demote other super admins");
        }

        UserRole oldRole = user.getUserRole();
        user.setUserRole(newRole);

        // Clear verification if demoting below VERIFIED
        if (!newRole.hasPermissionLevel(UserRole.VERIFIED)) {
            user.setVerifiedAt(null);
            user.setVerificationType(null);
        }

        User savedUser = saveUser(user);

        // Log activity with details
        Map<String, Object> details = new HashMap<>();
        details.put("oldRole", oldRole.name());
        details.put("newRole", newRole.name());
        details.put("reason", reason);
        details.put("demotedUsername", user.getUsername());

        activityLogService.logActivity(
                demotedBy,
                "USER_DEMOTION",
                "User",
                userId,
                details);

        // Notify the demoted user
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type("USER_DEMOTED")
                .message("Your account role has been changed to " + newRole.getDescription() +
                        (reason != null ? ". Reason: " + reason : ""))
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .build();

        notificationService.createNotification(notificationRequest);

        log.info("User ID: {} successfully demoted from {} to {}", userId, oldRole, newRole);
    }

    @Override
    @Transactional
    public void suspendUser(Long userId, String reason, LocalDateTime suspendUntil, Long suspendedBy) {
        log.info("Suspending user ID: {} until: {} by user ID: {} for reason: {}",
                userId, suspendUntil, suspendedBy, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        User suspender = userRepository.findById(suspendedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Suspender not found with id: " + suspendedBy));

        if (!suspender.canModerate()) {
            throw new SecurityException("Insufficient permissions to suspend user");
        }

        // Can't suspend admins unless you're a super admin
        if (user.hasMinimumRole(UserRole.ADMIN) && !suspender.hasRole(UserRole.SUPER_ADMIN)) {
            throw new SecurityException("Only super admins can suspend administrators");
        }

        user.setActive(false);
        user.setSuspendedUntil(suspendUntil);
        user.setSuspensionReason(reason);

        User savedUser = saveUser(user);

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("suspendUntil", suspendUntil != null ? suspendUntil.toString() : "indefinite");
        details.put("suspendedUsername", user.getUsername());

        activityLogService.logActivity(
                suspendedBy,
                "USER_SUSPENDED",
                "User",
                userId,
                details);

        // Notify the suspended user
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type("USER_SUSPENDED")
                .message("Your account has been suspended" +
                        (reason != null ? ". Reason: " + reason : "") +
                        (suspendUntil != null ? ". Suspension expires: " + suspendUntil : ""))
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .build();

        notificationService.createNotification(notificationRequest);

        log.info("User ID: {} successfully suspended", userId);
    }

    @Override
    @Transactional
    public void unsuspendUser(Long userId, Long unsuspendedBy) {
        log.info("Unsuspending user ID: {} by user ID: {}", userId, unsuspendedBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        User unsuspender = userRepository.findById(unsuspendedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Unsuspender not found with id: " + unsuspendedBy));

        if (!unsuspender.canModerate()) {
            throw new SecurityException("Insufficient permissions to unsuspend user");
        }

        user.setActive(true);
        user.setSuspendedUntil(null);
        user.setSuspensionReason(null);

        User savedUser = saveUser(user);

        // Log activity
        Map<String, Object> details = new HashMap<>();
        details.put("unsuspendedUsername", user.getUsername());

        activityLogService.logActivity(
                unsuspendedBy,
                "USER_UNSUSPENDED",
                "User",
                userId,
                details);

        // Notify the user
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type("USER_UNSUSPENDED")
                .message("Your account suspension has been lifted. Welcome back!")
                .relatedEntityId(userId)
                .relatedEntityType("USER")
                .build();

        notificationService.createNotification(notificationRequest);

        log.info("User ID: {} successfully unsuspended", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getModerators() {
        log.debug("Fetching all moderators");

        List<UserRole> moderatorRoles = Arrays.asList(
                UserRole.MODERATOR, UserRole.ADMIN, UserRole.SUPER_ADMIN);

        List<User> users = userRepository.findByUserRoleIn(moderatorRoles);
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getSuspendedUsers() {
        log.debug("Fetching suspended users");

        List<User> users = userRepository.findByIsActiveFalse();
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUsersNeedingModeration() {
        log.debug("Fetching users that may need moderation review");

        // This could be users with recent reports, suspicious activity, etc.
        // For now, return recently registered users
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<User> users = userRepository.findByCreatedAtAfterAndUserRole(oneWeekAgo, UserRole.USER);
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleStatistics getRoleStatistics() {
        log.debug("Generating user role statistics");

        long totalUsers = userRepository.count();
        long regularUsers = userRepository.countByUserRole(UserRole.USER);
        long verifiedUsers = userRepository.countByUserRole(UserRole.VERIFIED);
        long moderators = userRepository.countByUserRole(UserRole.MODERATOR);
        long admins = userRepository.countByUserRole(UserRole.ADMIN);
        long superAdmins = userRepository.countByUserRole(UserRole.SUPER_ADMIN);
        long suspendedUsers = userRepository.countByIsActiveFalse();

        return UserRoleStatistics.builder()
                .totalUsers(totalUsers)
                .regularUsers(regularUsers)
                .verifiedUsers(verifiedUsers)
                .moderators(moderators)
                .admins(admins)
                .superAdmins(superAdmins)
                .suspendedUsers(suspendedUsers)
                .build();
    }

    // Auto-unsuspend users whose suspension has expired
    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void processExpiredSuspensions() {
        log.debug("Processing expired user suspensions");

        LocalDateTime now = LocalDateTime.now();
        List<User> expiredSuspensions = userRepository.findBySuspendedUntilBeforeAndIsActiveFalse(now);

        for (User user : expiredSuspensions) {
            user.setActive(true);
            user.setSuspendedUntil(null);
            user.setSuspensionReason(null);

            saveUser(user);

            // Notify user
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(user.getId())
                    .type("SUSPENSION_EXPIRED")
                    .message("Your account suspension has expired. Welcome back!")
                    .relatedEntityId(user.getId())
                    .relatedEntityType("USER")
                    .build();

            notificationService.createNotification(notificationRequest);

            log.info("Auto-unsuspended user ID: {} - suspension expired", user.getId());
        }

        if (!expiredSuspensions.isEmpty()) {
            log.info("Processed {} expired suspensions", expiredSuspensions.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getRecentlyActiveUsers(int limit) {
        log.debug("Fetching {} recently active users", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findByIsActiveTrueOrderByLastLoginAtDesc(pageable);
        return userMapper.toSummaryResponseList(users);
    }

    // Helper methods
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null || followerId.equals(followingId)) {
            return false;
        }

        try {
            User follower = userRepository.findById(followerId).orElse(null);
            User following = userRepository.findById(followingId).orElse(null);

            if (follower == null || following == null) {
                return false;
            }

            return followRepository.existsByFollowerAndFollowing(follower, following);

        } catch (Exception e) {
            log.warn("Error checking follow relationship between user {} and {}: {}",
                    followerId, followingId, e.getMessage());
            return false;
        }
    }
}
