package com.armycommunity.service.user;

import com.armycommunity.dto.request.user.UserRegistrationRequest;
import com.armycommunity.dto.request.user.UserUpdateRequest;
import com.armycommunity.dto.response.user.UserProfileResponse;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // TODO: implement
        User user = new User();
        return userRepository.save(user);
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        // TODO: implement
        return null;
    }

    @Override
    public UserProfileResponse getUserProfileByUsername(String username) {
        // TODO: implement
        return null;
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        // TODO: implement
    }

    @Override
    public Page<UserProfileResponse> searchUsers(String keyword, Pageable pageable) {
        // TODO: implement
        return null;
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
        // TODO: implement
        return null;
    }

}
