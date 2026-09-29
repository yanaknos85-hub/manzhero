package ru.sber.transport.limits.providers.database;

import ru.sber.transport.database.limits.tables.records.DepartmentRecord;
import ru.sber.transport.limits.model.Department;

import java.util.UUID;

public record TestDepartment(UUID id, UUID parentId) implements Department {

    public TestDepartment(DepartmentRecord department) {
        this(department.getId(), department.getParentId());
    }

}