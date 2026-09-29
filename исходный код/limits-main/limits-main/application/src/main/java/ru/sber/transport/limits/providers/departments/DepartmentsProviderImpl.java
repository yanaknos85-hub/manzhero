package ru.sber.transport.limits.providers.departments;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.tables.records.DepartmentRecord;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.limits.business.model.Department;
import ru.sber.transport.limits.business.providers.DepartmentsProvider;
import ru.sber.transport.limits.providers.departments.mapper.DepartmentsDatabaseMapper;
import ru.sber.transport.limits.providers.employees.mappers.EmployeeDatabaseMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sber.transport.database.limits.Tables.EMPLOYEE;
import static ru.sber.transport.database.limits.Tables.LIMIT;

@Repository
@RequiredArgsConstructor
class DepartmentsProviderImpl implements DepartmentsProvider, ru.sber.transport.limits.web.providers.DepartmentsProvider, JooqRepository<ru.sber.transport.database.limits.tables.Department, DepartmentRecord, UUID> {

    private final DepartmentsDatabaseMapper mapper;

    private final EmployeeDatabaseMapper employeeDatabaseMapper;

    @Override
    public Optional<Department> get(UUID departmentId) {
        return findById(departmentId).map(mapper::toBusiness);
    }

    @Override
    public ru.sber.transport.database.limits.tables.Department table() {
        return ru.sber.transport.database.limits.tables.Department.DEPARTMENT;
    }

    @Override
    public Map<UUID, Department> getOfLimits(List<UUID> ids) {
        var result = context().select()
                .from(table())
                .innerJoin(LIMIT).on(LIMIT.DEPARTMENT_ID.eq(table().ID))
                .where(LIMIT.ID.in(ids))
                .fetchMap(LIMIT.ID, new RecordMapper<Record, Department>() {

                    @Nullable
                    @Override
                    public Department map(Record record) {
                        return mapper.toBusiness(record.into(DepartmentRecord.class));
                    }

                });
        var resultMap = result.values().parallelStream().collect(Collectors.toMap(Department::getId, Function.identity(), (l, r) -> l));
        context().select()
                .from(EMPLOYEE)
                .where(EMPLOYEE.DEPARTMENT_ID.in(result.keySet()))
                .forEach(it -> {
                    resultMap.get(it.getValue(table().ID)).setDepartmentHead(employeeDatabaseMapper.toBusiness(it.into(EmployeeRecord.class)));
                });
        return result;
    }
}
