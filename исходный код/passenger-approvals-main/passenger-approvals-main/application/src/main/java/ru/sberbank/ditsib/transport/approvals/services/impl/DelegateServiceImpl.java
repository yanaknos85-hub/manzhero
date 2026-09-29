package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.database.dao.DelegateRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Delegate;
import ru.sberbank.ditsib.transport.approvals.services.DelegateService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Реализация сервиса работы с делегатами.
 */
@RequiredArgsConstructor
@Transactional
@Component
public class DelegateServiceImpl implements DelegateService {
    
    private final DelegateRepository delegateRepository;
    
    @Override
    public Optional<Delegate> get(UUID id) {
        return delegateRepository.findById(id);
    }

    @Override
    public Set<Delegate> getDelegatesBySupervisor(UUID supervisorEmployeeId) {
        return delegateRepository.findAllBySupervisorId(supervisorEmployeeId, now());
    }
    
    private LocalDate now() {
        return LocalDate.now(ZoneId.of(ZoneOffset.UTC.getId()));
    }
    
    @Override
    public void delete(Delegate delegate) {
        delegateRepository.delete(delegate);
    }
    
    @Override
    public void save(Delegate delegate) {
        delegateRepository.save(delegate);
    }
    
}