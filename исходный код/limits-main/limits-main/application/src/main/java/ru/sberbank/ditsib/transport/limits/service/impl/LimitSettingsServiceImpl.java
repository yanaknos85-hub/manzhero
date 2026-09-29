package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.constants.SettingsNames;
import ru.sberbank.ditsib.transport.limits.dao.LimitSettingsRepository;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSettings;
import ru.sberbank.ditsib.transport.limits.service.LimitSettingsService;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LimitSettingsServiceImpl implements LimitSettingsService {
    
    private final LimitSettingsRepository limitSettingsRepository;
    
    @Override
    @Transactional
    public LimitSettings add(SettingsNames name, Object value) {
        if (value == null) {
            return null;
        }
        LimitSettings limitSettings = new LimitSettings();
        limitSettings.setName(name);
        limitSettings.setValue(String.valueOf(value));
        LimitSettings result;
        try {
            result = limitSettingsRepository.save(limitSettings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public LimitSettings save(LimitSettings limitSettings) {
        return limitSettingsRepository.save(limitSettings);
    }
    
    @Override
    @Transactional
    public void delete(SettingsNames name) {
        try {
            limitSettingsRepository.delete(get(name));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public LimitSettings get(SettingsNames name) {
        return limitSettingsRepository.findById(name).orElse(null);
    }
    
    @Override
    public String getByName(SettingsNames name) {
        return limitSettingsRepository.findById(name).map(LimitSettings::getValue).orElse(null);
    }
    
    @Override
    public List<LimitSettings> getAll() {
        return limitSettingsRepository.findAll();
    }

}
