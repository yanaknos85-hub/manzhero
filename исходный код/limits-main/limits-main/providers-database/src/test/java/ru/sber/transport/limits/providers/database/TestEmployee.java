package ru.sber.transport.limits.providers.database;

import ru.sber.transport.database.limits.tables.records.EmployeeRecord;

import java.util.UUID;

public record TestEmployee(UUID id, UUID departmentId) implements ru.sber.transport.limits.model.Employee {

    public TestEmployee(EmployeeRecord employee) {
        this(employee.getId(), employee.getDepartmentId());
    }

}