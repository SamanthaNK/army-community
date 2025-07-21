package com.armycommunity.service.setting;

import com.armycommunity.model.user.Setting;

import java.util.List;
import java.util.Map;

public interface SettingService {
    void saveSetting(Long userId, String key, String value);

    String getUserSetting(Long userId, String key);

    List<Setting> getUserSettings(Long userId);

    Map<String, String> getUserSettingsAsMap(Long userId);

    void deleteSetting(Long userId, String key);

    void saveGlobalSetting(String key, String value);

    String getGlobalSetting(String key);

    List<Setting> getAllGlobalSettings();

    Map<String, String> getGlobalSettingsAsMap();

    String getSettingWithGlobalFallback(Long userId, String key);

    void initializeUserSettings(Long userId);

    Map<String, String> getDefaultSettings();
}
