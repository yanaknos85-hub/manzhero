package ru.sber.transport.limits.web.http.resolvers.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Импортер данных лимитов организации.
 */
@RequiredArgsConstructor
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Transactional
@Slf4j
class OrganizationDataImporter {

    private final Map<String, Limit> depLimits = new HashMap<>();

    private final Map<String, Set<Limit>> parents = new HashMap<>();

    private final Set<LimitDataFileDto> added = new HashSet<>();

    private final Map<String, Department> departments = new HashMap<>();

    private final Map<UUID, Department> departmentUuids = new HashMap<>();

    @Setter
    private UUID author;

    private UUID organizationId;

    private final EmployeeRepository employeeRepository;

    private final LimitRepository<Limit> limitRepository;

    private final LimitSharingRepository limitSharingRepository;

    private final LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;

    private final DepartmentRepository departmentRepository;

    private final OrganizationRepository organizationRepository;

    @Qualifier("sQGeneratorLimits")
    private final SQGenerator sqGenerator;

    @Getter
    @Setter
    private String serviceType;

    @Getter
    @Setter
    private Integer year;

    /**
     * Установка идентификатора организации. Должен быть вызван до импорта.
     *
     * @param organizationId идентификатор организации.
     */
    void setOrganizationId(UUID organizationId) {
        if (this.organizationId != null) {
            throw new IllegalStateException("Importer for organization %s already initialized".formatted(organizationId));
        }
        this.organizationId = organizationId;
        var allDepartments = departmentRepository.findAllByOrganizationIdAndActiveIsTrue(organizationId);
        departments.putAll(allDepartments
                .parallelStream()
                .collect(Collectors.toMap(Department::getCode, Function.identity())));
        departmentUuids.putAll(allDepartments
                .parallelStream()
                .collect(Collectors.toMap(Department::getId, Function.identity())));
    }

    /**
     * Определение наличия данных в импортере.
     *
     * @return признак пустоты.
     */
    boolean isEmpty() {
        return depLimits.values().isEmpty();
    }

    /**
     * Получение подразделения по коду.
     *
     * @param departmentCode код подразделения.
     * @return подразделение.
     */
    Department getDepartment(String departmentCode) {
        return departments.get(departmentCode);
    }

    /**
     * Добавление элемента в импорт.
     *
     * @param source исходные данные.
     * @return флаг успешности добавления элемента. Добавление не успешно если данные по лимиту подразделения уже прогружались.
     */
    public boolean add(LimitDataFileDto source) {
        if (added.contains(source)) {
            return false;
        }
        switch (source.getLimitType()) {
            case "EMP" -> createEmpLimit(source);
            case "DEP" -> createDepLimit(source);
            default -> throw new IllegalArgumentException("Limit type %s is unknown".formatted(source.getLimitType()));
        }
        added.add(source);
        return true;
    }

    /**
     * Фиксирование данных в базе.
     */
    @Transactional
    public void persist() {
        if (isEmpty()) {
            throw new IllegalStateException("Файл импорта пуст");
        }
        added.clear();
        var upperLevel = parents.get(null).iterator().next();
        parents.remove(null);
        upperLevel = preparingTree(upperLevel);
        var stopwatch = new StopWatch();
        stopwatch.start("Persisting");
        persist(List.of(upperLevel));
        stopwatch.stop();

        var lastTaskInfo = stopwatch.lastTaskInfo();
        var lastTime = Duration.ofMillis(lastTaskInfo.getTimeMillis());
        log.info("%s took %d:%02d:%02d.%03d".formatted(lastTaskInfo.getTaskName(), lastTime.toHoursPart(), lastTime.toMinutesPart(), lastTime.toSecondsPart(), lastTime.toMillisPart()));
    }

    private Limit preparingTree(Limit limit) {
        if (limit instanceof DepLimit depLimit) {
            var departmentCode = depLimit.getDepartment().getCode();
            var children = Optional.ofNullable(parents.remove(departmentCode)).orElse(new HashSet<>())
                    .stream()
                    .map(this::preparingTree).toList();
            depLimit.setChildren(children);
            parents.remove(departmentCode);
        }
        return limit;
    }

    private List<Limit> persist(List<Limit> limits) {
        var referenceById = organizationRepository.getReferenceById(organizationId);

        var result = new ArrayList<Limit>();
        for (var limit : limits) {
            Prefix prefix = null;
            limit.setAuthor(employeeRepository.getReferenceById(limit.getAuthor().getId()));
            limit.setParentDepartment(Optional.ofNullable(limit.getParentDepartment()).map(Department::getId).map(departmentRepository::getReferenceById).orElse(null));
            limit.setParent(Optional.ofNullable(limit.getParent()).map(Limit::getId).map(limitRepository::getReferenceById).orElse(null));
            limit.setLimitOwner(Optional.ofNullable(limit.getLimitOwner()).map(Employee::getId).map(employeeRepository::getReferenceById).orElse(null));
            limit.setOrganization(referenceById);
            if (limit instanceof DepLimit depLimit) {
                depLimit.setDepartment(departmentRepository.getReferenceById(depLimit.getDepartment().getId()));
                prefix = Prefix.LD;
            } else if (limit instanceof EmpLimit empLimit) {
                empLimit.setEmployee(employeeRepository.getReferenceById(empLimit.getEmployee().getId()));
                prefix = Prefix.LU;
            }

            var humanReadableId = sqGenerator.getNextId(prefix, referenceById.getDigitId());
            limit.setHumanReadableId(humanReadableId);

            var sharings = limit.getSharings();
            var children = limit.getChildren();

            limit.setSharings(new ArrayList<>());
            limit.setChildren(new ArrayList<>());
            limit.setId(null);
            limit.setParent(null);

            var childrenSum = BigDecimal.ZERO;
            if (!children.isEmpty()) {
                var savedChildren = persist(children);

                limit.setSum(
                        savedChildren.stream()
                                .map(Limit::getSum)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            } else {
                limit.setSum(
                        sharings.stream()
                                .map(LimitSharing::getSum)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            }

            if (limit.getSum().compareTo(childrenSum) == 0) {
                log.debug("Overriding limit sum with children total: {} → {}", limit.getSum(), childrenSum);
            }

            var savedLimit = limitRepository.save(limit);
            log.debug("Saved limit {} with sum {} for {}", savedLimit.getHumanReadableId(), savedLimit.getSum(),
                    limit instanceof DepLimit d ? "DEP" + d.getDepartment().getCode() : "EMP");

            if (!children.isEmpty()) {
                for (var child : children) {
                    child.setParent(savedLimit);
                    limitRepository.save(child);
                }
            }

            for (var sharing : sharings) {
                sharing.setLimit(savedLimit);
                sharing.setAuthor(savedLimit.getAuthor());
                var periods = sharing.getSharingPerPeriods();
                sharing.setSharingPerPeriods(new ArrayList<>());
                var savedSharing = limitSharingRepository.save(sharing);

                for (var period : periods) {
                    period.setAuthor(savedSharing.getAuthor());
                    period.setLimitSharing(savedSharing);
                }
                limitSharingPerPeriodRepository.saveAll(periods);
            }

            result.add(savedLimit);
        }
        return result;
    }

    private void createDepLimit(LimitDataFileDto source) {
        Supplier<Department> parent = () -> {
            var department = departments.get(source.getDepartmentCode());
            if (department == null) {
                throw new EntityNotFoundException(Department.class, Map.of("code", source.getDepartmentCode()));
            }
            return Optional.ofNullable(department.getParentId()).map(departmentUuids::get).orElse(null);
        };
        var code = source.getDepartmentCode();

        var limit = createLimit(() -> (DepLimit) depLimits.getOrDefault(code, new DepLimit()), source, parent);
        limit.getSharings().add(createLimitSharing(source, limit));
        depLimits.put(code, limit);
        var parentCode = Optional.ofNullable(limit.getParentDepartment()).map(Department::getCode).orElse(null);
        if (!parents.containsKey(parentCode)) {
            parents.put(parentCode, new HashSet<>());
        }
        parents.get(parentCode).add(limit);
    }

    private void createEmpLimit(LimitDataFileDto source) {
        var limit = createLimit(EmpLimit::new, source, () -> employeeRepository.findByPersonnelNumber(source.getDepartmentCode()).stream().map(Employee::getDepartmentId).map(departmentUuids::get).findFirst().orElseThrow());
        limit.setEmployee(employeeRepository.findByPersonnelNumber(source.getDepartmentCode()).stream().filter(e -> e.getOrganizationId().equals(organizationId)).findFirst().orElseThrow());
        var parent = limit.getParent().getParentDepartment().getCode();
        if (!parents.containsKey(parent)) {
            parents.put(parent, new HashSet<>());
        }
        parents.get(parent).add(limit);
    }

    private <L extends Limit> L createLimit(Supplier<L> newLimitSupplier, LimitDataFileDto source, Supplier<Department> parent) {
        var curAuthor = Employee.builder().id(this.author).build();
        var limit = newLimitSupplier.get();
        if (limit.getId() == null) {
            limit.setId(UUID.randomUUID());
        }
        limit.setHumanReadableId(null);
        limit.setAuthor(curAuthor);
        limit.setLimitStatus(LimitStatus.SHARED);
        limit.setYear(source.getYear());
        limit.setLimitType(switch (source.getLimitType()) {
            case "EMP" -> LimitType.EMPLOYEE;
            case "DEP" -> LimitType.DEPARTMENT;
            default -> throw new IllegalArgumentException("Limit type %s is unknown".formatted(source.getLimitType()));
        });
        limit.setLimitSharingType(LimitSharingType.MONTHLY);
        limit.setLimitServiceType(serviceType);
        limit.setSum(source.getSum());
        limit.setFinalSharing(false);
        limit.setUseThisLimit(false);
        limit.setCreationTime(LocalDateTime.now(ZoneOffset.UTC));
        limit.setOrganization(Organization.builder().id(UUID.randomUUID()).build());

        var parentDepartment = parent.get();
        if (parentDepartment != null) {
            var parentLimit = depLimits.get(parentDepartment.getCode());
            limit.setParent(parentLimit);
            limit.setParentDepartment(parentDepartment);
        }
        if (limit instanceof DepLimit depLimit) {
            var department = departments.get(source.getDepartmentCode());
            depLimit.setDepartment(department);
            depLimit.setLimitOwner(department.getDepartmentHead());
        }
        return limit;
    }

    private LimitSharing createLimitSharing(LimitDataFileDto source, Limit limit) {
        var limitSharing = new LimitSharing();
        limitSharing.setAuthor(limit.getAuthor());
        limitSharing.setCreationTime(LocalDateTime.now(ZoneOffset.UTC));
        limitSharing.setSum(source.getSum());
        limitSharing.setBalance(source.getSum());
        limitSharing.setTransportType(TransportTypeEnum.valueOf(source.getTransportType()));
        limitSharing.setLimit(limit);
        limitSharing.setDistributed(true);

        var limitSharingPerPeriods = createLimitSharingPerPeriods(limitSharing);
        limitSharing.getSharingPerPeriods().addAll(limitSharingPerPeriods);
        return limitSharing;
    }

    private Collection<? extends LimitSharingPerPeriod> createLimitSharingPerPeriods(LimitSharing limitSharing) {
        var periods = switch (limitSharing.getLimit().getLimitSharingType()) {
            case MONTHLY, PERCENTS -> Month.values();
            case QUARTER -> Quarter.values();
        };
        var sharings = new LinkedList<LimitSharingPerPeriod>();
        var sum = limitSharing.getSum().divide(BigDecimal.valueOf(periods.length), 2, RoundingMode.HALF_EVEN);
        var remains = limitSharing.getSum();
        for (var period : periods) {
            var sharing = new LimitSharingPerPeriod();
            sharing.setAuthor(limitSharing.getAuthor());
            sharing.setCreationTime(LocalDateTime.now(ZoneOffset.UTC));
            sharing.setSum(sum);
            sharing.setBalance(sum);
            sharing.setLimitSharing(limitSharing);
            sharing.setPeriod(period);
            sharing.setAdditionalSum(BigDecimal.ZERO);
            remains = remains.subtract(sum);

            sharings.add(sharing);
        }
        var last = sharings.getLast();
        last.setSum(last.getSum().add(remains));
        last.setBalance(last.getSum());
        return sharings;
    }
}
