package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.limits.dao.DeadlineSettingsRepository;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.limits.service.DeadlineSettingsService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeadlineSettingsServiceImpl implements DeadlineSettingsService {
    
    private final DeadlineSettingsRepository repository;
    
    @Override
    public Optional<DeadlineSettings> getOptional(UUID settingsId) {
        return repository.findById(settingsId);
    }
    
    @Override
    public DeadlineSettings save(DeadlineSettings settings) {
        return repository.save(settings);
    }
    
    @Override
    public void delete(DeadlineSettings settings) {
        repository.delete(settings);
    }
    
}
