package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.approvals.database.dao.DepLimitRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.DepLimit;
import ru.sberbank.ditsib.transport.approvals.services.DepLimitService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса работы с лимитами департамента.
 */
@RequiredArgsConstructor
@Component
public class DepLimitServiceImpl implements DepLimitService {
    
    private final DepLimitRepository repository;

    @Override
    public Optional<DepLimit> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void save(DepLimit limit) {
        repository.save(limit);
    }

}
