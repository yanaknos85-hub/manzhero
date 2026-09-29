package ru.sber.transport.limits.providers.database.departments;

import lombok.RequiredArgsConstructor;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.tables.Department;
import ru.sber.transport.database.limits.tables.records.DepartmentRecord;
import ru.sber.transport.limits.providers.Departments;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DepartmentsImpl implements Departments, JooqRepository<Department, DepartmentRecord, UUID> {

    private final Departments additional;

    @Override
    public Department table() {
        return Tables.DEPARTMENT;
    }

    @Override
    public ru.sber.transport.limits.model.Department get(UUID id) {
        return findById(id)
                .map(this::toBusiness)
                .orElseGet(() -> save(additional.get(id)));
    }

    private ru.sber.transport.limits.model.Department save(ru.sber.transport.limits.model.Department source) {
        final var saved = context().insertInto(table())
                .set(table().ID, source.id())
                .set(table().PARENT_ID, source.parentId())
                .onConflict().doUpdate().setAllToExcluded().returning().fetchOne();
        return Optional.ofNullable(saved).map(this::toBusiness).orElseGet(() -> get(source.id()));
    }

    private ru.sber.transport.limits.model.Department toBusiness(DepartmentRecord departmentRecord) {
        return new ru.sber.transport.limits.model.Department() {

            @Override
            public UUID id() {
                return departmentRecord.getId();
            }

            @Override
            public UUID parentId() {
                return departmentRecord.getParentId();
            }
        };
    }
}
