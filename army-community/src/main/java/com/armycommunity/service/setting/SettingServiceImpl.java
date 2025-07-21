package com.armycommunity.service.setting;

import com.armycommunity.model.user.Setting;
import com.armycommunity.repository.user.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    private static final Map<String, String> DEFAULT_USER_SETTINGS = Map.of(
            "theme", "light",
            "language_preference", "en",
            "notifications_enabled", "true",
            "email_notifications", "true",
            "time_zone", "UTC",
            "post_privacy", "public",
            "show_online_status", "true"
    );

    @Override
    @Transactional
    public void saveSetting(Long userId, String key, String value) {
        log.debug("Saving user setting - userId: {}, key: {}, value: {}", userId, key, value);
        Optional<Setting> existingSetting = settingRepository.findByUserIdAndSettingKey(userId, key);

        if (existingSetting.isPresent()) {
            Setting setting = existingSetting.get();
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(setting);
            log.debug("Updated user setting - userId: {}, key: {}", userId, key);
        } else {
            Setting newSetting = Setting.builder()
                    .user(userId)
                    .settingKey(key)
                    .settingValue(value)
                    .isGlobal(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            settingRepository.save(newSetting);
            log.debug("Created new user setting - userId: {}, key: {}", userId, key);
        }
    }

    @Override
    public String getUserSetting(Long userId, String key) {
        log.debug("Fetching user setting - userId: {}, key: {}", userId, key);
        return settingRepository.findByUserIdAndSettingKey(userId, key)
                .map(Setting::getSettingValue)
                .orElse(DEFAULT_USER_SETTINGS.get(key));
    }

    @Override
    public List<Setting> getUserSettings(Long userId) {
        log.debug("Retrieving all settings for user: {}", userId);
        return settingRepository.findByUserId(userId);
    }

    @Override
    public Map<String, String> getUserSettingsAsMap(Long userId) {
        List<Setting> settings = getUserSettings(userId);
        Map<String, String> settingsMap = new HashMap<>();

        for (Setting setting : settings) {
            settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
        }

        log.debug("Retrieved {} settings for user: {}", settingsMap.size(), userId);
        return settingsMap;
    }

    @Override
    @Transactional
    public void deleteSetting(Long userId, String key) {
        log.debug("Deleting user setting - userId: {}, key: {}", userId, key);
        Optional<Setting> setting = settingRepository.findByUserIdAndSettingKey(userId, key);

        if (setting.isPresent()) {
            settingRepository.delete(setting.get());
            log.info("Deleted user setting - userId: {}, key: {}", userId, key);
        } else {
            log.warn("Attempted to delete non-existent setting - userId: {}, key: {}", userId, key);
        }
    }

    @Override
    @Transactional
    public void saveGlobalSetting(String key, String value) {
        log.debug("Saving global setting - key: {}, value: {}", key, value);
        Optional<Setting> existingSetting = settingRepository.findByUserIdAndSettingKey(null, key)
                .filter(Setting::getIsGlobal);

        if (existingSetting.isPresent()) {
            Setting setting = existingSetting.get();
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(setting);
            log.info("Updated global setting - key: {}", key);
        } else {
            Setting newSetting = Setting.builder()
                    .user(null)
                    .settingKey(key)
                    .settingValue(value)
                    .isGlobal(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            settingRepository.save(newSetting);
            log.info("Created new global setting - key: {}", key);
        }
    }

    @Override
    public String getGlobalSetting(String key) {
        log.debug("Fetching global setting - key: {}", key);
        return settingRepository.findByIsGlobalTrue().stream()
                .filter(setting -> key.equals(setting.getSettingKey()))
                .map(Setting::getSettingValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Setting> getAllGlobalSettings() {
        log.debug("Retrieving all global settings");
        return settingRepository.findByIsGlobalTrue();
    }

    @Override
    public Map<String, String> getGlobalSettingsAsMap() {
        List<Setting> settings = getAllGlobalSettings();
        Map<String, String> settingsMap = new HashMap<>();

        for (Setting setting : settings) {
            settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
        }

        log.debug("Retrieved {} global settings", settingsMap.size());
        return settingsMap;
    }

    // Helper method to get setting with fallback to global then default
    @Override
    public String getSettingWithGlobalFallback(Long userId, String key) {
        String userSetting = settingRepository.findByUserIdAndSettingKey(userId, key)
                .map(Setting::getSettingValue)
                .orElse(null);

        if (userSetting != null) {
            return userSetting;
        }

        String globalSetting = getGlobalSetting(key);
        if (globalSetting != null) {
            log.debug("Using global fallback for setting - userId: {}, key: {}", userId, key);
            return globalSetting;
        }

        String defaultSetting = DEFAULT_USER_SETTINGS.get(key);
        if (defaultSetting != null) {
            log.debug("Using default fallback for setting - userId: {}, key: {}", userId, key);
        }
        return defaultSetting;
    }

    @Override
    @Transactional
    public void initializeUserSettings(Long userId) {
        log.info("Initializing default settings for user: {}", userId);

        for (Map.Entry<String, String> entry : DEFAULT_USER_SETTINGS.entrySet()) {
            String key = entry.getKey();
            String defaultValue = entry.getValue();

            // Only create if setting doesn't already exist
            if (!settingRepository.findByUserIdAndSettingKey(userId, key).isPresent()) {
                Setting setting = Setting.builder()
                        .user(userId)
                        .settingKey(key)
                        .settingValue(defaultValue)
                        .isGlobal(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                settingRepository.save(setting);
            }
        }

        log.info("Default settings initialized for user: {}", userId);
    }

    @Override
    public Map<String, String> getDefaultSettings() {
        return new HashMap<>(DEFAULT_USER_SETTINGS);
    }

    // Helper method for common user preference settings
    public String getUserTheme(Long userId) {
        return getSettingWithGlobalFallback(userId, "theme");
    }

    public boolean areNotificationsEnabled(Long userId) {
        String setting = getSettingWithGlobalFallback(userId, "notifications_enabled");
        return setting == null || Boolean.parseBoolean(setting); // Default to true
    }

    public String getUserLanguage(Long userId) {
        return getSettingWithGlobalFallback(userId, "language_preference");
    }

    public String getUserTimezone(Long userId) {
        return getSettingWithGlobalFallback(userId, "time_zone");
    }
}
