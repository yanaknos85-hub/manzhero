package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsGrpcProvider;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DepartmentsServiceImpl implements DepartmentsService {

    private final DepartmentsDatabaseProvider departmentsDatabaseProvider;
    private final DepartmentsGrpcProvider departmentsGrpcProvider;
    private final EmployeesService employeesService;

    public DepartmentsServiceImpl(DepartmentsDatabaseProvider departmentsDatabaseProvider, DepartmentsGrpcProvider departmentsGrpcProvider, EmployeesService employeesService) {
        this.departmentsDatabaseProvider = departmentsDatabaseProvider;
        this.departmentsGrpcProvider = departmentsGrpcProvider;
        this.employeesService = employeesService;
        CompletableFuture.runAsync(() -> {
            log.info("Check consistency");
            Optional<UUID> checkId;
            do {
                checkId = departmentsDatabaseProvider.getWithoutName();
                checkId.ifPresent(it -> {
                    log.info("Fix inconsistency for {}", it);
                    createOrUpdate(departmentsGrpcProvider.get(it));
                });
            } while (checkId.isPresent());
        });
    }

    @Override
    public Department createOrUpdate(Department source) {
        final var department = departmentsDatabaseProvider.createOrUpdate(source);

        if (department.getHeadId() != null) {
            CompletableFuture.runAsync(() -> employeesService.getExistedOrCreate(department.getHeadId()));
        }
        if (department.getParentId() != null) {
            CompletableFuture.runAsync(() -> departmentsGrpcProvider.get(department.getParentId()));
        }

        return department;
    }

    @Override
    public Department getExistedOrCreate(UUID id) {
        final var department = departmentsDatabaseProvider.get(id);
        if (department != null) {
            return department;
        }

        final var requestedDepartment = departmentsGrpcProvider.get(id);
        final var saved = createOrUpdate(requestedDepartment);
        log.info("Responded department {} saved", id);
        return saved;
    }
}
