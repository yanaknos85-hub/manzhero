package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.ExecutorGroupRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone_;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupField;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupParameters;
import ru.sberbank.ditsib.corpclient.exceptions.ExecutorGroupCreateValidateException;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.mapper.ExecutorGroupMapper;
import ru.sberbank.ditsib.corpclient.service.ExecutorGroupService;
import ru.sberbank.ditsib.corpclient.util.SortUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * Implementation of ExecutorGroupService
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ExecutorGroupServiceImpl implements ExecutorGroupService {

    private static final String EXECUTOR_GROUP_ID_FIELD = "executorGroupId";

    private final ExecutorGroupMapper mapper;
    private final ExecutorGroupRepository repository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    @Qualifier("sQGenerator")
    private final SQGenerator sqGenerator;

    @Override
    public ExecutorGroupDTO add(ExecutorGroup model) {

        if (repository.existsExecutorGroupByNameEqualsAndServiceEqualsAndActiveTrue(model.getName(), model.getService())) {
            throw new ExecutorGroupCreateValidateException("23011");
        }

        if (StringUtils.hasText(model.getServiceLevel())
                && repository.existsExecutorGroupByServiceLevelEqualsAndServiceEqualsAndActiveTrue(
                model.getServiceLevel(), model.getService())) {

            throw new ExecutorGroupCreateValidateException("23014");
        }

        List<ExecutorGroup> executorGroups = Optional.ofNullable(model.getDepartments())
                .filter(CollectionUtils::isNotEmpty)
                .map(departments -> repository.findByOrganizationsInAndDepartmentsInAndActiveTrue(model.getOrganizations(), departments))
                .orElseGet(() -> repository.findByOrganizationsInAndActiveTrue(model.getOrganizations()));

        if (executorGroups.stream().anyMatch(eg -> equalsTo(eg, model))) {
            throw new ExecutorGroupCreateValidateException("23017");
        }

        model.setId(UUID.randomUUID());
        model.setHumanReadableId(sqGenerator.getNextId(Prefix.EG,
                organizationRepository
                        .findById(model.getOrganizationId())
                        .orElseThrow(() -> new EntityNotFoundException(Organization.class, model.getOrganizationId()))
                        .getDigitId()));

        model.setActive(true);

        ExecutorGroup executorGroup = repository.saveAndFlush(model);

        List<String> exceptions = checkAddExecutorGroup(executorGroup);

        if (isNotEmpty(exceptions)) {
            throw new ExecutorGroupCreateValidateException(exceptions.toString());
        }

        return mapper.toDto(executorGroup);
    }

    @Override
    public ExecutorGroupDTO get(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new EntityNotFoundException(
                ExecutorGroup.class, Map.of(EXECUTOR_GROUP_ID_FIELD, id)));
    }

    @Override
    public ExecutorGroupDTO update(UUID executorGroupId, ExecutorGroup newData) {
        ExecutorGroup executorGroup = repository.findById(executorGroupId).orElseThrow(() -> new EntityNotFoundException(
                ExecutorGroup.class, Map.of(EXECUTOR_GROUP_ID_FIELD, executorGroupId)));

        executorGroup.setService(newData.getService());
        executorGroup.setName(newData.getName());
        executorGroup.setOrganizationId(newData.getOrganizationId());
        executorGroup.setServiceLevel(newData.getServiceLevel());
        executorGroup.setExecutors(newData.getExecutors());
        executorGroup.setOrganizations(newData.getOrganizations());
        executorGroup.setDepartments(newData.getDepartments());
        executorGroup.setCustomers(newData.getCustomers());
        executorGroup.setGeoZones(newData.getGeoZones());
        executorGroup.setAdditionalFeature(newData.getAdditionalFeature());
        executorGroup.setContractors(newData.getContractors());

        return mapper.toDto(repository.saveAndFlush(executorGroup));
    }
    @Override
    public ExecutorGroupDTO delete(@NotNull UUID executorGroupId) {
        var executorGroup = repository.findById(executorGroupId).orElseThrow(() -> new EntityNotFoundException(
                ExecutorGroup.class, Map.of(EXECUTOR_GROUP_ID_FIELD, executorGroupId)));
        executorGroup.setActive(false);
        return mapper.toDto(repository.saveAndFlush(executorGroup));
    }

    @Override
    public Iterable<ExecutorGroupDTO> get(ExecutorGroupParameters parameters) {
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        var spec = new ExecutorGroupFilterSpecification(parameters.getFilter());
        return repository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(mapper::toDto);
    }

    @Override
    public ExecutorGroup getExecutorGroupByEmployeeId(
            UUID employeeId,
            List<UUID> geoZoneIds,
            String service) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));

        var executorGroups = repository.findByOrganizationsInAndServiceAndActiveTrue(Set.of(employee.getOrganization()), service);

        var groupsByGeoZoneIn = executorGroups.stream()
                .filter(eg -> eg.getGeoZones().stream()
                        .map(GeoZone::getId)
                        .anyMatch(geoZoneIds::contains))
                .findFirst();

        if (groupsByGeoZoneIn.isPresent()) return groupsByGeoZoneIn.get();

        var result = executorGroups.stream()
                .filter(eg -> eg.getCustomers().stream()
                        .map(Employee::getId)
                        .anyMatch(c -> c.equals(employee.getId())))
                .findFirst();

        if (result.isPresent()) return result.get();

        executorGroups.removeIf(eg -> isNotEmpty(eg.getCustomers()));

        result = executorGroups.stream()
                .filter(eg -> eg.getDepartments().stream()
                        .map(Department::getId)
                        .anyMatch(d -> d.equals(employee.getDepartment().getId())))
                .findFirst();
        if(result.isPresent()) return result.get();

        executorGroups.removeIf(eg -> isNotEmpty(eg.getDepartments()));

        result = executorGroups.stream()
                .filter(eg -> eg.getOrganizations().stream()
                        .map(Organization::getId)
                        .anyMatch(o -> o.equals(employee.getOrganization().getId())))
                .findFirst();

        if(result.isPresent()) return result.get();

        return repository.findByNameAndActiveTrue("default").findFirst().orElse(null);
    }

    private List<String> checkAddExecutorGroup(ExecutorGroup executorGroup) {

        List<String> exceptions = new ArrayList<>();

        var executorGroupOrganizationIds = executorGroup.getOrganizations()
                .stream()
                .map(Organization::getId)
                .collect(Collectors.toSet());

        for (Employee executor : executorGroup.getExecutors()) {
            if (notEqual(executor.getOrganization().getId(), executorGroup.getOrganizationId())) {
                exceptions.add("23001");
            }
            if (notEqual(executor.getDepartment().getOrganization().getId(), executorGroup.getOrganizationId())) {
                exceptions.add("23003");
            }
        }

        if (executorGroup.getDepartments().stream().map(Department::getOrganization).map(Organization::getId)
                .anyMatch(dId -> !executorGroupOrganizationIds.contains(dId))) {

            exceptions.add("23006");
        }

        if (executorGroup.getCustomers().stream().map(Employee::getOrganization).map(Organization::getId)
                .anyMatch(dId -> !executorGroupOrganizationIds.contains(dId))) {

            exceptions.add("23009");
        }

        return exceptions;
    }

    private boolean equalsTo(ExecutorGroup executorGroup, ExecutorGroup executorGroupCompare) {
        return Objects.equals(executorGroup.getService(), executorGroupCompare.getService())
                && Objects.equals(executorGroup.getOrganizations(), executorGroupCompare.getOrganizations())
                && Objects.equals(executorGroup.getDepartments(), Optional.ofNullable(executorGroupCompare.getDepartments()).orElse(Collections.emptySet()))
                && Objects.equals(executorGroup.getCustomers(), Optional.ofNullable(executorGroupCompare.getCustomers()).orElse(Collections.emptySet()));
    }

    /**
     * Спецификация для фильтрации группы исполнителей.
     */
    @RequiredArgsConstructor
    private static class ExecutorGroupFilterSpecification implements Specification<ExecutorGroup> {

        private final Map<ExecutorGroupField, Serializable> filter;

        @Override
        public Predicate toPredicate(Root<ExecutorGroup> root,
                                     CriteriaQuery<?> query,
                                     CriteriaBuilder criteriaBuilder) {

            var predicate = criteriaBuilder.isNotNull(root.get(ExecutorGroup_.HUMAN_READABLE_ID));

            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get(ExecutorGroup_.ACTIVE), Boolean.TRUE));

            if (filter != null) {
                for (var entry : filter.entrySet()) {
                    var value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    predicate =
                            switch (entry.getKey()) {
                                case EXECUTOR_GROUP_NAME -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(ExecutorGroup_.NAME),
                                                "%" + value + "%"));
                                case EXECUTOR_FIO -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.join(ExecutorGroup_.EXECUTORS).get(Employee_.LAST_NAME),
                                                value + "%"));
                                case EXECUTOR_PERSONAL_NUMBER -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.equal(root.join(ExecutorGroup_.EXECUTORS)
                                                .get(Employee_.PERSONNEL_NUMBER), value));
                                case EXECUTOR_ORGANIZATION -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.equal(root.get(ExecutorGroup_.ORGANIZATION_ID),
                                                value));
                                case CUSTOMER_ORGANIZATION -> criteriaBuilder.and(predicate,
                                        root.join(ExecutorGroup_.ORGANIZATIONS).get(Organization_.ID)
                                                .in(Collections.unmodifiableCollection((ArrayList) value)));
                                case CUSTOMER_DEPARTMENT -> criteriaBuilder.and(predicate,
                                        root.join(ExecutorGroup_.DEPARTMENTS).get(Department_.ID)
                                                .in(Collections.unmodifiableCollection((ArrayList) value)));
                                case SERVICE_TYPE -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(ExecutorGroup_.SERVICE),
                                                "%" + value + "%"));
                                case CUSTOMER_GEO_ZONE -> criteriaBuilder.and(predicate,
                                        root.join(ExecutorGroup_.GEO_ZONES).get(GeoZone_.ID)
                                                .in(Collections.unmodifiableCollection((ArrayList) value)));
                                case ID -> criteriaBuilder.and(predicate,
                                        criteriaBuilder.like(root.get(ExecutorGroup_.HUMAN_READABLE_ID),
                                                "%" + value + "%"));
                            };
                }
            }
            query.distinct(true);
            return predicate;
        }
    }
}
