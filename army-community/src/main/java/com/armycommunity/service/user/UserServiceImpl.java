package com.armycommunity.service.user;

import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserProfileResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.UserMapper;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.user.UserRepository;
import com.armycommunity.service.activitylog.ActivityLogService;
import com.armycommunity.service.filestorage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private FileStorageService fileStorageService;
    private ActivityLogService activityLogService;
    private static final String PROFILE_IMAGES_DIR = "profile-images";

    @Override
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create a new User
        User user = userMapper.toEntity(request);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Only set password for non-OAuth users
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setBio(request.getBio());
        user.setRole("USER");
        user.setLanguagePreference(request.getLanguagePreference() != null ? request.getLanguagePreference() : "en");
        user.setTimezone(request.getTimeZone() != null ? request.getTimeZone() : "UTC");
        user.setOauthProvider(request.getOauthProvider());
        user.setOauthId(request.getOauthId());

        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Log activity
        activityLogService.logActivity(
                savedUser.getId(),
                "REGISTRATION",
                "USER",
                savedUser.getId(),
                null
        );

        return savedUser;
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toProfileResponse(user);
    }

    @Override
    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if username is being changed and if it already exists
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
        }

        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Use mapper to update other fields
        userMapper.updateEntity(request, user);

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        // Log activity
        activityLogService.logActivity(
                updatedUser.getId(),
                "PROFILE_UPDATE",
                "USER",
                updatedUser.getId(),
                null
        );

        return updatedUser;
    }

    @Override
    public User updateUserProfileImage(Long userId, MultipartFile imageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete old profile image if exists
        if (user.getProfileImagePath() != null) {
            fileStorageService.deleteFile(user.getProfileImagePath());
        }

        // Save new profile image
        String imagePath = fileStorageService.storeFile(imageFile, PROFILE_IMAGES_DIR);
        user.setProfileImagePath(imagePath);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Instead of actually deleting, mark as inactive
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Log activity
        activityLogService.logActivity(
                userId,
                "ACCOUNT_DEACTIVATION",
                "USER",
                userId,
                null
        );
    }

    @Override
    public Page<UserProfileResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> userPage = new PageImpl<>(userRepository.searchUsers(keyword), pageable,
                userRepository.searchUsers(keyword).size());

        return userPage.map(userMapper::toProfileResponse);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserProfileResponse> getRecentlyActiveUsers(int limit) {
        // just returns the most recently created users
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .sorted((u1, u2) -> u2.getLastLoginAt().compareTo(u1.getLastLoginAt()))
                .limit(limit)
                .map(userMapper::toProfileResponse)
                .collect(Collectors.toList());
    }

}
