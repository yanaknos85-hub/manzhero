package ru.sber.transport.corporate.business.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.corporate.business.Employees;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.*;
import ru.sber.transport.corporate.web.grpc.client.SyncClient;
import ru.sber.transport.dto.Page;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.web.model.Projection;
import ru.sberbank.ditsib.request.Direction;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
@RequiredArgsConstructor
class EmployeesImpl implements Employees {

    private final Provider<Employee, EmployeeFilter> provider;

    private final Provider<Position, PositionFilter> positionProvider;

    private final DepartmentProvider departmentProvider;

    private final AttributeProvider attributeProvider;

    private final ApprovalsProvider approvalsProvider;

    private final SyncClient client;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    @SneakyThrows({InterruptedException.class})
    @Override
    public Employee get(UUID employeeId) {
        var employee = provider.get(employeeId).orElseGet(() -> client.findEntered().orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId)));

        Future<Position> positionFuture;
        Future<List<UUID>> departmentsFuture;
        Future<List<Attribute>> attributesFuture;
        Future<Integer> approvalsFuture;
        try (var executor = Executors.newFixedThreadPool(4)) {
            positionFuture = executor.submit(() -> positionProvider.get(employee.getPositionId()).orElseThrow(() -> new EntityNotFoundException(Position.class, employee.getPositionId())));
            departmentsFuture = executor.submit(() -> departmentProvider.findOfHead(employeeId));
            attributesFuture = executor.submit(() -> attributeProvider.findOfEmployee(employeeId));
            approvalsFuture = executor.submit(() -> approvalsProvider.countOfEmployee(employeeId));
        }
        try {
            employee.setApprovals(approvalsFuture.get());
            employee.setAttributes(attributesFuture.get());
            employee.setDepartmentHead(!departmentsFuture.get().isEmpty());
            employee.setManagedDepartments(departmentsFuture.get());
            employee.setPositionName(positionFuture.get().getName());
        } catch (ExecutionException e) {
            throw (RuntimeException) e.getCause();
        }
        return employee;
    }

    @Override
    @SuppressWarnings("java:S2201")
    public Page<Employee> get(@NonNull EmployeeFilter filter, @NonNull Projection projection, @NonNull Integer page, @NonNull Integer size, @NonNull String sort, @NonNull Direction direction) {
        final var loggedIn = ControllerUtils.currentUser();
        final var userOrganizationId = employeeOrganizationFunction.apply(loggedIn);
        final var dataMaster = ControllerUtils.isDataMaster();
        if (!dataMaster) {
            final var organizationsOpt = Optional.ofNullable(filter.getOrganizations());
            final var departmentsOpt =  Optional.ofNullable(filter.getDepartments());
            final var organizationDepartments = departmentsOpt
                    .map(departmentProvider::getOrganizations)
                    .orElseGet(Collections::emptyMap);

            organizationsOpt
                    .map(ids -> ids.removeIf(id -> !id.equals(userOrganizationId)));

            departmentsOpt
                    .map(ids -> ids.removeIf(id -> !organizationDepartments.get(id).equals(userOrganizationId)));
        }
        return provider.streamAll(filter, projection, page, size, sort, direction);
    }
}
