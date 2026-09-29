package ru.sber.transport.limits.business.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.dto.Page;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.limits.business.Limits;
import ru.sber.transport.limits.business.exceptions.ClosingUpperLevelNotAllowedException;
import ru.sber.transport.limits.business.exceptions.UpdateNotAllowedException;
import ru.sber.transport.limits.business.model.*;
import ru.sber.transport.limits.business.providers.*;
import ru.sberbank.ditsib.request.Direction;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
class LimitsImpl implements Limits {

    private final LimitsProvider limits;

    private final LimitSharingProvider limitSharings;

    private final LimitSharingPerPeriodProvider limitSharingPerPeriod;

    private final EmployeeProvider employees;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    private final DepartmentsProvider departments;

    public LimitsImpl(LimitsProvider limits, LimitSharingProvider limitSharings, LimitSharingPerPeriodProvider limitsPerPeriodProvider, EmployeeProvider employees, Clock clock, @Value("${next_month.cron:0 0 1 * * *}") String nextMonthCron, EmployeeOrganizationFunction employeeOrganizationFunction, DepartmentsProvider departments) {
        this.limits = limits;
        this.limitSharings = limitSharings;
        this.limitSharingPerPeriod = limitsPerPeriodProvider;
        this.employees = employees;
        this.employeeOrganizationFunction = employeeOrganizationFunction;
        this.departments = departments;

        var scheduler = new ThreadPoolTaskSchedulerBuilder()
                .awaitTermination(true)
                .poolSize(1)
                .threadNamePrefix("Next month - ")
                .build();
        scheduler.initialize();
        scheduler.schedule(() -> processLimits(clock), new CronTrigger(nextMonthCron));
    }

    void processLimits(Clock clock) {
        var nextDate = LocalDate.now(clock);
        if (Month.JANUARY.equals(nextDate.getMonth())) {
            log.debug("Moving limits between year is unallowed");
            return;
        }
        var previousDate = nextDate.minusMonths(1);
        var previousLimit = limitSharingPerPeriod.getNotMoved(previousDate)
                .parallelStream()
                .map(it -> new AbstractMap.SimpleEntry<>(it.getLimitSharingId(), it))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var nextLimit = limitSharingPerPeriod.get(nextDate)
                .parallelStream()
                .map(it -> new AbstractMap.SimpleEntry<>(it.getLimitSharingId(), it))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        log.debug("Transferring {} limits from {} to {}", previousLimit.size(), previousDate, nextDate);
        previousLimit.entrySet().parallelStream().forEach(it -> moveLimit(it.getValue(), nextLimit.get(it.getKey())));
    }

    private void moveLimit(LimitSharingPerPeriod previous, LimitSharingPerPeriod next) {
        final var balance = previous.getBalance();
        log.debug("{}) Moving {} from {} to {}", previous.getLimitSharingId(), balance, previous.getPeriod(), next.getPeriod());
        next.setBalance(next.getBalance().add(balance));
        next.setSum(next.getSum().add(balance));
        next.setAdditional(balance);
        previous.setBalance(BigDecimal.ZERO);
        previous.setSum(balance);
        previous.setMovedToNext(true);
        limitSharingPerPeriod.save(next);
        limitSharingPerPeriod.save(previous);
    }

    @Override
    public void delete(UUID limitId, UUID userId, boolean forceAllow) {
        var limit = limits.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        if (!forceAllow && !checkUser(userId, limit)) {
            throw new UnauthorizedException(userId);
        }
        log.debug("Find limit with id: {}", limitId);
        if (limit.getParentId() == null) {
            throw new ClosingUpperLevelNotAllowedException(limitId);
        }
        var parent = limits.upper(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limit.getParentId()));
        try (var limitsCloser = Executors.newVirtualThreadPerTaskExecutor()) {
            var children = limits.getChildren(limitId);
            log.debug("Closing {} children of {}", children.size(), limitId);
            children.stream().peek(it -> {
                log.debug("Returning sum {} into upper-level reserve", limit.getSum());
                parent.setReserve(parent.getReserve().add(it.getSum()));
                it.setSum(BigDecimal.ZERO);
                it.setReserve(BigDecimal.ZERO);
                it.setStatus(Status.CLOSED);
            }).forEach(it -> limitsCloser.submit(() -> closeLimit(it)));
        }
        limits.save(parent);
    }

    @Override
    public @NonNull Limit get(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId) {
        final var limit = limits.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        checkAccess(userId, forceAllow, limit);
        return limit;
    }

    @Override
    public @NonNull ModifiedLimit get(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince) {
        final var limit = limits.get(limitId, modifiedSince).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        checkAccess(userId, forceAllow, limit.data());
        return limit;
    }

    @Override
    public @NonNull Limit hash(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId) {
        final var limit = limits.hash(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        checkAccess(userId, forceAllow, limit);
        return limit;
    }

    @Override
    public Limit update(UUID limitId, UUID userId, boolean forceAllow, Limit newData, List<String> updatedFields) {
        final var limit = limits.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        checkAccess(userId, forceAllow, limit);
        if (updatedFields.parallelStream().anyMatch(it -> it.endsWith("sum") || updatedFields.contains("reserve")) && !Status.PLANNING.equals(limit.getStatus())) {
            final var changed = new ArrayList<String>(2);
            updatedFields.parallelStream().filter(it -> it.endsWith("sum")).findFirst().ifPresent(it -> checkField(changed, it, newData.getSum(), limit.getSum()));
            updatedFields.parallelStream().filter(it -> it.endsWith("reserve")).findFirst().ifPresent(it -> checkField(changed, it, newData.getReserve(), limit.getReserve()));
            if (!changed.isEmpty()) {
                throw new UpdateNotAllowedException(limit.getStatus(), changed.toArray(String[]::new));
            }
        }
        for (var responsible : newData.getResponsibles()) {
            if (!checkUser(responsible)) {
                throw new EntityNotFoundException(Employee.class, responsible);
            }
        }
        return limits.update(limit, newData, updatedFields);
    }

    @Override
    public Page<Limit> get(@NonNull LimitFilter filter, @NonNull Integer page, @NonNull Integer size, @NonNull String sort, @NonNull Direction direction) {
        final var departmentId = filter.getDepartmentId();
        final var currentUser = employees.current();
        final var dataMaster = ControllerUtils.isDataMaster();
        final var organizationId = filter.getOrganizationId() != null || dataMaster ? filter.getOrganizationId() : employeeOrganizationFunction.apply(currentUser.getId());
        final var department = Optional.ofNullable(departmentId).map(id -> departments.get(id).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId)));
        if (department.isPresent() && department.get().getOrganizationId() != organizationId && !employees.hasAccess()) {
            throw new UnauthorizedException(currentUser.getId());
        }
        final var searchOrganizationId = department.map(Department::getOrganizationId).orElse(organizationId);
        return limits.get(searchOrganizationId, filter, page, size, sort, direction);
    }

    private static void checkField(List<String> changed, String fieldName, BigDecimal newSum, BigDecimal oldSum) {
        final var fieldNameParts = fieldName.split(":");
        final var op = fieldNameParts[0];
        final var fieldNameEffective = fieldNameParts[1];
        var checkSum = newSum;
        if ("ADD".equals(op)) {
            checkSum = oldSum.add(checkSum);
        }
        if ("REMOVE".equals(op)) {
            checkSum = oldSum.subtract(oldSum);
        }
        if (!checkSum.equals(oldSum)) {
            changed.add(fieldNameEffective);
        }
    }

    @Override
    public @NonNull ModifiedLimit hash(@NonNull UUID userId, boolean forceAllow, @NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince) {
        final var limit = limits.hash(limitId, modifiedSince).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        checkAccess(userId, forceAllow, limit.data());
        return limit;
    }

    private void closeLimit(Limit limit) {
        var limitId = limit.getId();
        log.trace("Closing limit with id: {}", limitId);
        var sharings = limitSharings.getAll(limitId);
        log.debug("Closing {} sharings of {}", sharings.size(), limitId);
        sharings.forEach(this::closeLimitSharing);
        limits.save(limit);
    }

    private void closeLimitSharing(LimitSharing limitSharing) {
        var limitSharingId = limitSharing.getId();
        var periods = limitSharingPerPeriod.getAll(limitSharingId);
        log.debug("Closing {} periods of {}", periods.size(), limitSharingId);
        periods.forEach(it -> {
            it.setSum(it.getSum().subtract(it.getBalance()));
            it.setBalance(BigDecimal.ZERO);
            limitSharingPerPeriod.save(it);
        });
        limitSharing.setSum(limitSharing.getSum().subtract(limitSharing.getRemains()));
        limitSharing.setRemains(BigDecimal.ZERO);
        limitSharings.save(limitSharing);
    }

    private boolean checkUser(UUID userId) {
        return employees.get(userId).isPresent();
    }

    private boolean checkUser(UUID userId, Limit limit) {
        return employees.get(userId).filter(it -> it.getOrganizationId().equals(limit.getOrganizationId())).isPresent();
    }

    private void checkAccess(@NotNull UUID userId, boolean forceAllow, Limit limit) {
        if (!forceAllow && !employeeOrganizationFunction.apply(userId).equals(limit.getOrganizationId())) {
            throw new UnauthorizedException(userId);
        }
    }

}