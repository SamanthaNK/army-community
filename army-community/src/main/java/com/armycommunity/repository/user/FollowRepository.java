package com.armycommunity.repository.user;

import com.armycommunity.model.user.User;

public interface FollowRepository {
    boolean existsByFollowerAndFollowing(User follower, User following);
}
