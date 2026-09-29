package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.limits.constants.LimitHistoryType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitInfoDto;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitNotSufficientException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.LimitHistoryService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;
import ru.sberbank.ditsib.transport.limits.service.LimitTransferHistoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LimitServiceImpl implements LimitService {

    private final LimitRepository<Limit> limitRepository;

    private final LimitSharingService limitSharingService;

    private final EmpLimitRepository empLimitRepository;

    private final DepLimitRepository depLimitRepository;

    private final LimitTransferHistoryService limitTransferHistoryService;

    private final LimitHistoryService limitHistoryService;

    private final DepartmentRepository departmentRepository;

    private final EmployeeRepository employeeRepository;

    @Override
    public Optional<Limit> get(UUID limitId) {
        return limitRepository.findById(limitId).map(this::addTransients);
    }

    @Override
    public Set<Limit> getLimitChildren(Limit limit, LimitType limitType) {
        if (limitType == null) {
            return limitRepository.findByParent(limit);
        } else {
            return limitRepository.findByParentAndLimitType(limit, limitType);
        }
    }

    @Override
    public List<Limit> getUpperLevelLimitList(UUID organizationId, String serviceType, Integer year) {
        return limitRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNull(organizationId, serviceType, year);
    }

    @Override
    public List<Limit> getUpperLevelActiveLimitList(UUID organizationId, String serviceType, Integer year) {
        return limitRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNullAndLimitStatusNot(organizationId, serviceType, year,
                LimitStatus.CLOSED);
    }

    @Override
    public List<Limit> getUpperLevelActiveLimitListNoChildren(UUID organizationId, String serviceType, Integer year) {
        return limitRepository.findByOrganizationIdAndLimitServiceTypeAndYearAndParentIdIsNullAndLimitStatusNotWithoutChildren(organizationId, serviceType, year,
                LimitStatus.CLOSED);
    }

    @Override
    public Optional<Limit> getUpperLevelActiveLimit(UUID organizationId, String serviceType, Integer year) {
        return getUpperLevelActiveLimitList(organizationId, serviceType, year).stream().findAny();
    }

    @Override
    public Optional<Limit> getUpperLevelActiveLimitNoChildren(UUID organizationId, String serviceType, Integer year) {
        return getUpperLevelActiveLimitListNoChildren(organizationId, serviceType, year).stream().findAny();
    }

    @Override
    public List<Limit> getUpperLevelActiveLimit(UUID organizationId, Integer year) {
        return limitRepository.findByOrganizationIdAndYearAndParentIdIsNull(organizationId, year);
    }

    @Override
    public List<Limit> getByOrganizationIdAndYear(UUID organizationId, Integer year) {
        return limitRepository.findByOrganizationIdAndYear(organizationId, year);
    }

    @Override
    public List<Limit> getUpperLevelActiveLimit(UUID organizationId) {
        return limitRepository.findByOrganizationIdAndParentIdIsNull(organizationId);
    }

    /**
     * Transfer sum from source department/transport_type to target department/transport_type.
     */
    @Override
    @Transactional
    public void transferSum(
            LimitData source, LimitData target,
            BigDecimal sum, UUID authorId,
            LimitTransferHistoryType limitTransferHistoryType,
            boolean allowTakingFromClosedSource
    ) {
        log.debug("DEBUG: transferSum started");
        var sourceLimit = source.limit();
        var targetLimit = target.limit();
        if (isDataInvalid(sum, allowTakingFromClosedSource, sourceLimit, targetLimit)) {
            return;
        }
        var sourceTransportType = source.transportType();
        var fromPeriod = source.period();
        var targetTransportType = target.transportType();
        var toPeriod = target.period();

        var sourceLimitSharing = limitSharingService.getByLimitAndTransportType(sourceLimit, sourceTransportType);
        if (sourceLimitSharing == null) {
            throw new EntityNotFoundException(LimitSharing.class, Map.of("id", sourceLimit.getId().toString(), "transportType", sourceTransportType));
        }
        var sourceLimitSharingPerPeriod = takeSumFromLimit(sourceLimit, sourceLimitSharing, sourceTransportType, sum, fromPeriod);

        var targetLimitSharing = limitSharingService.getByLimitAndTransportType(targetLimit, targetTransportType);
        if (targetLimitSharing == null) {
            throw new EntityNotFoundException(LimitSharing.class, Map.of("id", targetLimit.getId(), "transportType", targetTransportType));
        }
        var targetLimitSharingPerPeriod = addSumToLimit(targetLimit, targetLimitSharing, sum, toPeriod);

        limitTransferHistoryService.add(authorId, source, target, sum, sourceLimit.getYear(), limitTransferHistoryType);


        if (limitTransferHistoryType == LimitTransferHistoryType.GENERAL) {
            LimitHistoryType limitHistoryType = LimitHistoryType.TRANSFER_FROM_LIMIT;
            if (sourceLimit.getId().equals(targetLimit.getId())) {
                limitHistoryType = LimitHistoryType.TRANSFER_INSIDE_LIMIT_BETWEEN_TRANSPORT_TYPES;
            }
            limitHistoryService.add(authorId,
                    new LimitData(sourceLimit, sourceTransportType, fromPeriod),
                    new LimitData(targetLimit, targetTransportType, toPeriod),
                    sum, sourceLimit.getYear(),
                    limitHistoryType,
                    sourceLimitSharing,
                    sourceLimitSharingPerPeriod);
            limitHistoryType = LimitHistoryType.TRANSFER_TO_LIMIT;
            if (sourceLimit.getId().equals(targetLimit.getId())) {
                limitHistoryType = LimitHistoryType.TRANSFER_INSIDE_LIMIT_BETWEEN_TRANSPORT_TYPES;
            }
            limitHistoryService.add(authorId,
                    new LimitData(targetLimit, targetTransportType, toPeriod),
                    new LimitData(sourceLimit, sourceTransportType, fromPeriod),
                    sum.negate(), sourceLimit.getYear(),
                    limitHistoryType,
                    targetLimitSharing,
                    targetLimitSharingPerPeriod);
        }

        log.debug("DEBUG: transferSum finished");
    }

    private boolean isDataInvalid(BigDecimal sum, boolean allowTakingFromClosedSource, Limit sourceLimit, Limit targetLimit) {
        if (sourceLimit == null || targetLimit == null) {
            throw new LimitLogicException("transferSum: исходный или целевой лимит не найден!");
        }
        if (sourceLimit.getYear() != targetLimit.getYear()) {
            throw new LimitLogicException("transferSum: годы лимитов не совпадают");
        }
        if (!sourceLimit.getLimitServiceType().equals(targetLimit.getLimitServiceType())) {
            throw new LimitLogicException("transferSum: типы услуг не совпадают");
        }
        var sourceUpperLimit = getUpperParent(sourceLimit);
        var targetUpperLimit = getUpperParent(targetLimit);
        if (sourceUpperLimit == null && targetUpperLimit != null
                || sourceUpperLimit != null && targetUpperLimit == null
                || sourceUpperLimit != null && !sourceUpperLimit.getId().equals(targetUpperLimit.getId())) {
            throw new LimitLogicException("transferSum: исходный и целевой лимит из разных деревьев");
        }
        if (!allowTakingFromClosedSource && (sourceLimit.getLimitStatus().equals(LimitStatus.CLOSED))) {
            throw new LimitLogicException("transferSum: Исходный лимит закрыт. Limit id = " + sourceLimit.getId());
        }
        if (targetLimit.getLimitStatus().equals(LimitStatus.CLOSED)) {
            throw new LimitLogicException("transferSum: Целевой лимит закрыт. Limit id = " + targetLimit.getId());
        }
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new LimitLogicException("transferSum: Невозможно перевести отрицательную сумму!");
        }
        return sum.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Take sum from limit.
     */
    private LimitSharingPerPeriod takeSumFromLimit(Limit limit, LimitSharing limitSharing, TransportTypeEnum transportType, BigDecimal sum, Period fromPeriod) {
        log.trace("DEBUG: takeSumFromLimit started");
        LimitSharingPerPeriod limitSharingPerPeriod = null;

        // check if it has enough credit
        if (limitSharing.getBalance().compareTo(sum) < 0) {
            throw new LimitNotSufficientException(limitSharing.getLimit().getHumanReadableId(),
                    transportType);
        }
        var reversedSum = sum.negate();
        limitSharingService.changeLimitSharingSumAndBalance(limitSharing, reversedSum);
        // change limitsharing per period
        if (limitSharing.isDistributed()) {
            if (fromPeriod == null) {
                limitSharingService.distributeSharingPerPeriod(limitSharing);
            } else {
                limitSharingPerPeriod = limitSharingService.putToMonth(limitSharing, reversedSum, fromPeriod);
            }
        }

        // change limit
        changeLimitSum(limit, reversedSum);
        log.info("DEBUG: takeSumFromLimit finished");
        return limitSharingPerPeriod;
    }

    /**
     * Add sum to limit.
     */
    private LimitSharingPerPeriod addSumToLimit(Limit limit, LimitSharing limitSharing, BigDecimal sum, Period toPeriod) {
        log.trace("DEBUG: addSumToLimit started");
        LimitSharingPerPeriod limitSharingPerPeriod = null;
        limitSharingService.changeLimitSharingSumAndBalance(limitSharing, sum);
        // change limitsharing per period
        if (limitSharing.isDistributed()) {
            if (toPeriod == null) {
                limitSharingService.distributeSharingPerPeriod(limitSharing);
            } else {
                limitSharingPerPeriod = limitSharingService.putToMonth(limitSharing, sum, toPeriod);
            }
        }
        // change limit
        changeLimitSum(limit, sum);
        log.info("DEBUG: addSumToLimit finished");
        return limitSharingPerPeriod;
    }

    /**
     * Change limit sum for limit.
     */
    private void changeLimitSum(Limit limit, BigDecimal sum) {
        if (limit.getParent() != null) {
            limit.setSum(Optional.ofNullable(limit.getSum()).orElse(BigDecimal.ZERO).add(sum));
            if (limit instanceof EmpLimit empLimit) {
                empLimitRepository.save(empLimit);
            }
            if (limit instanceof DepLimit depLimit) {
                depLimitRepository.save(depLimit);
            }
        }
    }

    @Override
    public Map<UUID, Department> getDepartments(Set<UUID> limitIds) {
        Specification<DepLimit> spec = (root, cb, query) -> {
            root.fetch(DepLimit_.department);
            return root.get(Limit_.id).in(limitIds);
        };
        return depLimitRepository.findAll(spec).stream()
            .collect(Collectors.toMap(DepLimit::getId, DepLimit::getDepartment));
    }

    @Override
    public Map<UUID, List<Limit>> getChildren(Set<UUID> parentIds) {
        return limitRepository.findByParentIdInAndLimitStatusNot(parentIds, LimitStatus.CLOSED).stream().collect(Collectors.toMap(l -> l.getParent().getId(), List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));
    }

    @Override
    public List<Limit> getByLimitSharingType(LimitSharingType limitSharingType) {
        return limitRepository.findByLimitSharingType(limitSharingType);
    }

    @Override
    public GetLimitInfoDto getLimiInfoByDate(Employee employee, LocalDate date, TransportTypeEnum transportType) {
        log.info("getLimiInfoByDate employee = {}, department = {}, date = {}, type = {}", employee.getId(), employee.getDepartmentId(), date,
                transportType);
        int year = date.getYear();
        var months = Month.valueOf(date.getMonth());
        var result = GetLimitInfoDto.builder()
                .month(months.ordinal())
                .quarter(getQuarter(months))
                .transportType(transportType)
                .year(year);
        // личный лимит
        List<EmpLimit> empLimitList = empLimitRepository
                .findByEmployeeIdAndYearAndLimitStatusAndLimitType(employee.getId()
                        , year, LimitStatus.SHARED, LimitType.EMPLOYEE);
        // лимит подразделения
        List<DepLimit> depLimitList = depLimitRepository
                .findByDepartmentIdAndYearAndLimitStatusAndLimitType(employee.getDepartmentId()
                        , year, LimitStatus.SHARED, LimitType.DEPARTMENT);
        if (empLimitList.isEmpty()) {
            if (!depLimitList.isEmpty()) {
                var limitSharingPerPeriod = getSharingPerPeriod(depLimitList.getFirst(), transportType, months);
                return result.balance(limitSharingPerPeriod.getBalance())
                        .sum(limitSharingPerPeriod.getSum())
                        .limitSharingType(depLimitList.getFirst().getLimitSharingType())
                        .build();
            }
        } else {
            var limitSharingPerPeriod = getSharingPerPeriod(empLimitList.getFirst(), transportType, months);
            var limitSum = limitSharingPerPeriod.getSum();
            var limitBalance = limitSharingPerPeriod.getBalance();
            if (!depLimitList.isEmpty()) {
                var limitSharingPerPeriodDep = getSharingPerPeriod(depLimitList.getFirst(), transportType, months);
                return result.balance((limitBalance.compareTo(limitSharingPerPeriodDep.getBalance()) < 0 ? limitSharingPerPeriodDep.getBalance() : limitBalance))
                        .sum((limitSum.compareTo(limitSharingPerPeriodDep.getSum()) < 0 ? limitSharingPerPeriodDep.getSum() : limitSum))
                        .limitSharingType(depLimitList.getFirst().getLimitSharingType())
                        .build();
            }
            return result.balance(limitBalance)
                    .sum(limitSum)
                    .limitSharingType(empLimitList.getFirst().getLimitSharingType())
                    .build();
        }
        // нет личного лимита, нет лимита подразделения
        // ищем лимит по иерархии вверх с признаком "Использовать лимит моего подразделения"
        DepLimit depLimitUse = getDepartmentLimitWithUse(employee.getDepartmentId(), year,
                LimitServiceType.getLimitServiceTypeByTransportType(transportType).name());
        if (depLimitUse == null) {
            return result.balance(BigDecimal.ZERO).sum(BigDecimal.ZERO).limitSharingType(null).build();
        }
        var limitSharingPerPeriodDep = getSharingPerPeriod(depLimitUse, transportType, months);
        if (limitSharingPerPeriodDep == null) {
            return result.balance(BigDecimal.ZERO).sum(BigDecimal.ZERO).limitSharingType(null).build();
        }
        return result.balance(limitSharingPerPeriodDep.getBalance())
                .sum(limitSharingPerPeriodDep.getSum())
                .limitSharingType(depLimitUse.getLimitSharingType())
                .build();
    }

    @Override
    @Transactional
    public boolean closeLimit(Limit limit, UUID authorId) {
        if(limitRepository.checkHaveNotClosedChildLimits(limit.getId())) {
            throw new LimitLogicException("Trying to close limit with children: " + limit.getId());
        }
        if (limit.getLimitStatus() == LimitStatus.CLOSED) {
            return false;
        }
        if (limit.getParent() != null) {
            List<LimitSharing> limitSharingList = limitSharingService.getByLimit(limit);
            for (LimitSharing limitSharing : limitSharingList) {
                var source = new LimitData(limit, limitSharing.getTransportType());
                var target = new LimitData(limit.getParent(), limitSharing.getTransportType());
                transferSum(source, target, limitSharing.getBalance(), authorId, false);
            }
        }
        limitHistoryService.add(limit.getAuthor().getId(),
                new LimitData(limit, null, null),
                null,
                limit.getSum(), limit.getYear(),
                LimitHistoryType.CLOSE,
                null,
                null);
        limit.setLimitStatus(LimitStatus.CLOSED);
        if (limit instanceof EmpLimit empLimit) {
            empLimitRepository.save(empLimit);
        }
        if (limit instanceof DepLimit depLimit) {
            depLimitRepository.save(depLimit);
        }
        return true;
    }

    private DepLimit getDepartmentLimitWithUse(UUID departmentId, int year, String limitServiceType) {
        DepLimit limit = getByDepartmentAndYearAndLimitServiceType(departmentId, year, limitServiceType);
        if (limit != null && limit.isUseThisLimit()) {
            return limit;
        } else {
            Department department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) {
                log.error("ERROR: department not found for id {}", departmentId);
                return null;
            }
            if (department.getParentId() != null) {
                return getDepartmentLimitWithUse(department.getParentId(), year, limitServiceType);
            } else {
                return null;
            }
        }
    }

    public DepLimit getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType) {
        return depLimitRepository
                .findByDepartmentIdAndYearAndLimitServiceTypeAndLimitStatusNot(departmentId, year, limitServiceType, LimitStatus.CLOSED).orElse(null);
    }

    private LimitSharingPerPeriod getSharingPerPeriod(Limit limit, TransportTypeEnum transportType, Period months) {
        var sharing = limit.getSharings().stream()
                .filter(r -> transportType.equals(r.getTransportType()))
                .findFirst()
                .orElseThrow(() -> new LimitLogicException(String.format("Нет лимита для %s", transportType)));
        return sharing.getSharingPerPeriods().stream().filter(t -> Objects.equals(t.getPeriod(), months)).findAny()
                .orElseThrow(() -> new LimitLogicException("Нет лимита на указанную дату"));
    }

    /**
     * получить квартал по месяцу, начиная с 0
     *
     * @param period месяц
     * @return квартал
     */
    private int getQuarter(Month period) {
        return switch (period) {
            case JANUARY, FEBRUARY, MARCH -> 1;
            case APRIL, MAY, JUNE -> 2;
            case JULY, AUGUST, SEPTEMBER -> 3;
            case OCTOBER, NOVEMBER, DECEMBER -> 4;
        };
    }

    private DepLimit getUpperParent(Limit limit) {
        var parentLimit = limit;
        while (parentLimit.getParent() != null) {
            parentLimit = limitRepository.findById(parentLimit.getParent().getId()).orElse(null);
            if (parentLimit == null) {
                return null;
            }
        }
        if (parentLimit instanceof DepLimit depLimit) {
            return depLimit;
        }
        return depLimitRepository.findById(parentLimit.getId()).orElse(null);
    }

    private Limit addTransients(Limit limit) {
        if (limit instanceof DepLimit depLimit) {
            var department = depLimit.getDepartment();
            if (department.getDepartmentHead() != null) {
                department.setDepartmentHead(employeeRepository.findById(department.getDepartmentHead().getId()).orElse(null));
            }
        }
        return limit;
    }
}
