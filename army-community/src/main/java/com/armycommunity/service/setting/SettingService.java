package com.armycommunity.service.setting;

import com.armycommunity.model.user.Setting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SettingService {
    Setting saveSetting(Long userId, String key, String value);

    Optional<Setting> getUserSetting(Long userId, String key);

    List<Setting> getUserSettings(Long userId);

    Map<String, String> getUserSettingsAsMap(Long userId);

    void deleteSetting(Long userId, String key);

    Setting saveGlobalSetting(String key, String value);

    Optional<Setting> getGlobalSetting(String key);

    List<Setting> getAllGlobalSettings();

    Map<String, String> getGlobalSettingsAsMap();
}
