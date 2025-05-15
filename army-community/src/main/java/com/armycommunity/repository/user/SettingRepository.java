package com.armycommunity.repository.user;

import com.armycommunity.model.user.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    List<Setting> findByUserId(Long userId);

    Optional<Setting> findByUserIdAndSettingKey(Long userId, String settingKey);

    List<Setting> findByIsGlobalTrue();
}
