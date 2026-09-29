package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.TransportOrgRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.OrganizationField;
import ru.sberbank.ditsib.corpclient.messaging.sender.DepartmentSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.OrganizationSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Implementation of OrganizationService
 */
@Service
@Transactional
@RequiredArgsConstructor
class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository repository;

    private final DepartmentRepository departmentRepository;

    private final EmployeeRepository employeeRepository;

    private final UserSender userSender;

    private final EmployeeSender employeeSender;

    private final DepartmentSender departmentSender;

    private final OrganizationSender sender;

    private final TransportOrgRepository transportOrgRepository;

    @Override
    public Organization get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Organization.class, id));
    }

    @Override
    public Optional<Organization> get(String name) {
        return repository.findByOfficialName(name);
    }

    @Override
    public List<Organization> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        deleteOrganization(id);
    }

    @Override
    public Page<Organization> get(int page, int size, Sort sort, Map<OrganizationField, Serializable> filter) {
        var spec = new OrganizationSpecification(filter);
        return repository.findAll(spec, PageRequest.of(page, size, sort));
    }

    @Override
    public List<Organization> get(Sort sort, Map<OrganizationField, Serializable> filter) {
        var spec = new OrganizationSpecification(filter);
        return repository.findAll(spec, sort);
    }

    @Override
    public Organization add(Organization organization) {
        var syncId = organization.getSyncId();
        if (syncId != null && repository.existsBySyncId(syncId)) {
            throw new DuplicateDataException(Organization.class, "easupId", syncId);
        }
        var officialName = organization.getOfficialName();
        repository.findByOfficialName(officialName)
                .ifPresent(e -> {
                    throw new DuplicateDataException(Organization.class, "officialName", officialName);
                });
        organization.setId(null);
        organization.setSyncId(syncId);
        organization = repository.saveAndFlush(organization);
        sender.send(organization);
        return organization;
    }

    @Override
    public void edit(UUID organizationId, Organization newData) {
        var organization = get(organizationId);
        organization.getContacts().clear();
        repository.findByOfficialNameAndIdNot(newData.getOfficialName(), organizationId)
                .ifPresent(org -> {
                    throw new DuplicateDataException(Organization.class.getName(),
                            "officialName", newData.getOfficialName());
                });
        organization.setAddress(newData.getAddress());
        organization.setOfficialName(newData.getOfficialName());
        organization.setOrganizationCode(newData.getOrganizationCode());
        organization.setMsrn(newData.getMsrn());
        organization.setTid(newData.getTid());
        organization.getContacts().addAll(newData.getContacts());
        organization = repository.saveAndFlush(organization);

        final var classes = transportOrgRepository.getByOrganizationId(organization.getId());

        organization.setAvailableClasses(classes.stream().map(TransportOrg::getTransportType).toList());

        sender.send(organization);
    }

    @Override
    public boolean validateOrganizationId(@NotNull UUID organizationId) {
        if (!repository.existsById(organizationId)) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        return true;
    }

    @RequiredArgsConstructor
    private static class OrganizationSpecification implements Specification<Organization> {

        private final Map<OrganizationField, Serializable> filter;

        @Override
        public Predicate toPredicate(
                Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
        ) {
            root.join(Organization_.CONTACTS, JoinType.LEFT);
            var predicate = criteriaBuilder.equal(root.get(Organization_.STATUS), OrganizationStatus.ACTIVE);
            if (filter != null) {
                for (var entry : filter.entrySet()) {
                    var value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    predicate =
                            switch (entry.getKey()) {
                                case OFFICIAL_NAME -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(Organization_.OFFICIAL_NAME),
                                                value + "%"));
                                case ADDRESS -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(Organization_.ADDRESS),
                                                value + "%"));
                                case MSRN -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(Organization_.MSRN),
                                                value + "%"));
                                case TID -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(Organization_.TID),
                                                value + "%"));
                                case ORGANIZATION_CODE -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(Organization_.ORGANIZATION_CODE),
                                                value + "%"));
                                case GROUP_ID -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.equal(root.join(Organization_.ORGANIZATION_GROUP).get(OrganizationGroup_.ID), value));
                            };
                }
            }
            query.distinct(true);
            return predicate;
        }
    }

    //При удалении внешней организации с сохранением возможности ее
    //быстрого восстановления требуется деактивировать только ее сотрудников
    private void deleteOrganization(UUID id) {
        repository.findById(id).ifPresent(organization -> {
            try (final var executor = Executors.newFixedThreadPool(10)) {
                departmentRepository.findAllByOrganizationId(organization.getId())
                        .stream()
                        .forEach(department -> executor.submit(() -> {
                            department.setActiveStatus(ActiveStatus.INACTIVE);
                            departmentSender.send(departmentRepository.save(department));
                        }));
                employeeRepository.findAllByOrganizationId(organization.getId())
                        .stream()
                        .forEach(employee -> executor.submit(() -> {
                            employee.setActiveStatus(ActiveStatus.INACTIVE);
                            final var saved = employeeRepository.save(employee);
                            employeeSender.send(saved);
                            userSender.send(saved, null);
                        }));
                organization.setStatus(OrganizationStatus.INACTIVE);
                sender.send(repository.save(organization));
            }
        });
    }
}