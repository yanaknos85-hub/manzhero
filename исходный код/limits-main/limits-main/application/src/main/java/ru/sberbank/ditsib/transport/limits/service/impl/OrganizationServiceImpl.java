package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.service.OrganizationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of service for working with organization.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    
    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public List<Organization> getAllActive() {
        return repository.findAllByActive(true);
    }
    
    @Override
    @Transactional
    public void delete(Organization organization) {
        organization.setActive(false);
        repository.save(organization);
    }
    
    @Override
    @Transactional
    public Organization save(Organization organization) {
        return repository.save(organization);
    }
}
