package com.armycommunity.repository.user;

import com.armycommunity.model.user.User;
import com.armycommunity.model.user.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(String provider, String id); // TODO: use this method (implemented OAUTH)

    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);

    List<User> findByUserRole(UserRole userRole);

    List<User> findByUserRoleIn(List<UserRole> roles);

    List<User> findByVerifiedAtIsNotNull();

    Long countByUserRole(UserRole userRole);

    Long countByIsActiveFalse();

    // For suspension management
    List<User> findBySuspendedUntilBeforeAndIsActiveFalse(LocalDateTime dateTime);
    List<User> findByIsActiveFalse();

    // For moderation
    List<User> findByCreatedAtAfterAndUserRole(LocalDateTime dateTime, UserRole role);

    // Update your existing method to work with new role system
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.lastLoginAt DESC")
    List<User> findByIsActiveTrueOrderByLastLoginAtDesc(Pageable pageable);
}
