package com.armycommunity.service.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.armycommunity.model.user.Setting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingsService{

    @Override
    public Setting saveSetting(Long userId, String key, String value) {
        // TODO: implement
        return null;
    }

    @Override
    public Optional<Setting> getUserSetting(Long userId, String key) {
        // TODO: implement
        return java.util.Optional.empty();
    }

    @Override
    public List<Setting> getUserSettings(Long userId) {
        // TODO: implement
        return java.util.Collections.emptyList();
    }

    @Override
    public Map<String, String> getUserSettingsAsMap(Long userId) {
        // TODO: implement
        return java.util.Collections.emptyMap();
    }

    @Override
    public void deleteSetting(Long userId, String key) {
        // TODO: implement
    }

    @Override
    public Setting saveGlobalSetting(String key, String value) {
        // TODO: implement
        return null;
    }

    @Override
    public Optional<Setting> getGlobalSetting(String key) {
        // TODO: implement
        return java.util.Optional.empty();
    }

    @Override
    public List<Setting> getAllGlobalSettings() {
        // TODO: implement
        return java.util.Collections.emptyList();
    }

    @Override
    public Map<String, String> getGlobalSettingsAsMap() {
        // TODO: implement
        return java.util.Collections.emptyMap();
    }
}
