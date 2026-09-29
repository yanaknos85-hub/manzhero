package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.exception.AwaitingSynchronizationException;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.grpc.Organizations;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of service for working with organization.
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    private final Organizations organizations;
    
    @Override
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Organization getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Organization.class, id));
    }
    
    @Override
    public void delete(Organization entity) {
        repository.save(entity.setActive(false));
    }
    
    @Override
    public void save(Organization entity) {
        repository.save(entity);
    }

    @SneakyThrows
    @Override
    public void saveGrpcEntity(String message, UUID id) {
        this.save(Optional.ofNullable(organizations.one(id)).orElseThrow(() -> {
            log.info(message);
            throw new AwaitingSynchronizationException("Awaiting an organization synchronization");
        }));
    }
}
