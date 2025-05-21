package com.armycommunity.service.setting;

import com.armycommunity.model.user.Setting;
import com.armycommunity.repository.user.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService{

    private final SettingRepository settingRepository;

    @Override
    @Transactional
    public Setting saveSetting(Long userId, String key, String value) {
        Optional <Setting> existingSetting = settingRepository.findByUserIdAndSettingKey(userId, key);

        if (existingSetting.isPresent()) {
            // Update the existing setting
            Setting setting = existingSetting.get();
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            return settingRepository.save(setting);
        } else {
            // Create a new setting
            Setting newSetting = new Setting();
            newSetting.setUser(userId);
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setIsGlobal(false);
            newSetting.setCreatedAt(LocalDateTime.now());
            newSetting.setUpdatedAt(LocalDateTime.now());
            return settingRepository.save(newSetting);
        }
    }

    @Override
    public Optional<Setting> getUserSetting(Long userId, String key) {
        return settingRepository.findByUserIdAndSettingKey(userId, key);
    }

    @Override
    public List<Setting> getUserSettings(Long userId) {
        return settingRepository.findByUserId(userId);
    }

    @Override
    public Map<String, String> getUserSettingsAsMap(Long userId) {
        List<Setting> settings = settingRepository.findByUserId(userId);
        Map<String, String> settingsMap = new HashMap<>();

        for (Setting setting : settings) {
            settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
        }

        return settingsMap;
    }

    @Override
    @Transactional
    public void deleteSetting(Long userId, String key) {
        Optional<Setting> setting = settingRepository.findByUserIdAndSettingKey(userId, key);
        setting.ifPresent(settingRepository::delete);
    }

    @Override
    @Transactional
    public Setting saveGlobalSetting(String key, String value) {
        List<Setting> globalSettings = settingRepository.findByIsGlobalTrue();
        Optional<Setting> existingGlobalSetting = globalSettings.stream()
                .filter(s -> s.getSettingKey().equals(key))
                .findFirst();

        if (existingGlobalSetting.isPresent()) {
            // Update the existing global setting
            Setting setting = existingGlobalSetting.get();
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            return settingRepository.save(setting);
        } else {
            // Create a new global setting
            Setting newSetting = new Setting();
            newSetting.setUser(null);
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setIsGlobal(true);
            newSetting.setCreatedAt(LocalDateTime.now());
            newSetting.setUpdatedAt(LocalDateTime.now());
            return settingRepository.save(newSetting);
        }
    }

    @Override
    public Optional<Setting> getGlobalSetting(String key) {
        List<Setting> globalSettings = settingRepository.findByIsGlobalTrue();
        return globalSettings.stream()
                .filter(s -> s.getSettingKey().equals(key))
                .findFirst();
    }

    @Override
    public List<Setting> getAllGlobalSettings() {
        return settingRepository.findByIsGlobalTrue();
    }

    @Override
    public Map<String, String> getGlobalSettingsAsMap() {
        List<Setting> globalSettings = settingRepository.findByIsGlobalTrue();
        return globalSettings.stream()
                .collect(Collectors.toMap(Setting::getSettingKey, Setting::getSettingValue));
    }
}
