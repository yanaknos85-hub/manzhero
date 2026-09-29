package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.limits.constants.LimitSpendingStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSpending;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSpendingService;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsData;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
class LimitStatsDataImpl implements LimitStatsData {

    private final ExecutorService executors = Executors.newVirtualThreadPerTaskExecutor();

    private final LimitService limitService;

    private final EmployeeService employeeService;

    private final LimitSpendingService limitSpendingService;

    private CompletableFuture<Map<UUID, Department>> departmentTask;

    private CompletableFuture<Map<UUID, List<LimitSpending>>> spendingsMap;

    private CompletableFuture<Map<UUID, Long>> employeesCount;

    private LocalDate startOfYear;

    private LocalDate untilDate;

    @Override
    public void load(List<LimitLevelDTO> limitLevelDTOs, LocalDate untilDate) {
        this.untilDate = untilDate;
        this.startOfYear = untilDate != null ? LocalDate.of(untilDate.getYear(), Month.JANUARY, 1) : null;
        var limitIds = CompletableFuture.supplyAsync(() -> loadLimits(limitLevelDTOs), executors).thenApplyAsync(this::getLimitIds);
        departmentTask = CompletableFuture.supplyAsync(() -> getDepartmentsMap(getValue(limitIds)), executors);
        employeesCount = CompletableFuture.supplyAsync(() -> getComployeesCountMap(getValue(departmentTask)));
        spendingsMap = CompletableFuture.supplyAsync(() -> limitSpendingService.getByLimitAndStatusNot(getValue(limitIds), LimitSpendingStatus.CANCELED));
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public Department department(UUID limitId) {
        return departmentTask.get().get(limitId);
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public LinkedList<LimitSpending> popSpends(UUID limitId) {
        return new LinkedList<>(Optional.ofNullable(spendingsMap.get().get(limitId)).orElseGet(List::of));
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public long employees(UUID departmentId) {
        return Optional.ofNullable(employeesCount.get().get(departmentId)).orElse(0L);
    }

    @Override
    public LocalDate untilDate() {
        return untilDate;
    }

    @Override
    public LocalDate startOfYear() {
        return startOfYear;
    }

    private @NotNull Set<Limit> loadLimits(List<LimitLevelDTO> limitLevelDTOs) {
        return limitLevelDTOs.parallelStream().map(LimitLevelDTO::getLimit).collect(Collectors.toUnmodifiableSet());
    }

    private @NotNull Set<UUID> getLimitIds(Set<Limit> limits) {
        return limits.parallelStream().map(Limit::getId).collect(Collectors.toUnmodifiableSet());
    }

    private Map<UUID, Department> getDepartmentsMap(Set<UUID> limitIds) {
        return limitService.getDepartments(limitIds);
    }

    private Map<UUID, Long> getComployeesCountMap(Map<UUID, Department> depMap) {
        return employeeService.countByDepartmentsAndActive(depMap.values().parallelStream().map(Department::getId).collect(Collectors.toUnmodifiableSet()), true);
    }

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    private <T> T getValue(CompletableFuture<T> limitIds) {
        return limitIds.get();
    }
}
