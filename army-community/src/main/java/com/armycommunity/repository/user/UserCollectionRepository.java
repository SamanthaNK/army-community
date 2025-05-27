package com.armycommunity.repository.user;

import com.armycommunity.model.user.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing UserCollection entities.
 */
@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
    List<UserCollection> findByUserId(Long userId); // TODO: use this method

    List<UserCollection> findByAlbumId(Long albumId); // TODO: use this method

    @Query("SELECT COUNT(uc) FROM UserCollection uc WHERE uc.album.id = ?1")
    Integer countByAlbumId(Long albumId);
}