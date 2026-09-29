package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.business.PositionsService;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sber.transport.fraud.monitoring.providers.EmployeesDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.EmployeesGrpcProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class EmployeesServiceImpl implements EmployeesService {

    private final EmployeesDatabaseProvider employeesDatabaseProvider;
    private final EmployeesGrpcProvider employeesGrpcProvider;
    private final PositionsService positionsService;

    @Override
    public Employee createOrUpdate(Employee source) {
        final var employee = employeesDatabaseProvider.createOrUpdate(source);
        if (employee.getPositionId() != null) {
            CompletableFuture.runAsync(() -> positionsService.getExistedOrCreate(employee.getPositionId()));
        }
        return employeesDatabaseProvider.createOrUpdate(source);
    }

    @Override
    public Employee getExistedOrCreate(UUID id) {
        final var employee = employeesDatabaseProvider.get(id);
        if (employee != null) {
            return employee;
        }

        final var requestedEmployee = employeesGrpcProvider.get(id);
        final var saved = createOrUpdate(requestedEmployee);
        log.info("Responded employee {} saved", id);
        return saved;
    }
}
