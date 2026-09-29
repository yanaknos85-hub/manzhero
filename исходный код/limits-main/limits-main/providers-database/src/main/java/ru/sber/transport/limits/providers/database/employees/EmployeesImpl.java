package ru.sber.transport.limits.providers.database.employees;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.limits.model.Employee;
import ru.sber.transport.limits.providers.Employees;

import java.util.Optional;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
public class EmployeesImpl implements Employees, JooqRepository<ru.sber.transport.database.limits.tables.Employee, EmployeeRecord, UUID> {

    private final Employees additional;

    @Override
    public Employee get(UUID id) {
        return findById(id)
                .map(this::createEmployee)
                .orElseGet(() -> {
                    final var found = additional.get(id);
                    return save(found);
                });
    }

    private Employee save(Employee employee) {
        final var saved = context().insertInto(table())
                .set(table().ID, employee.id())
                .set(table().DEPARTMENT_ID, employee.departmentId())
                .onConflict(table().ID)
                .doUpdate()
                .setAllToExcluded()
                .returning()
                .fetchOne();
        return Optional.ofNullable(saved).or(() -> findById(employee.id())).map(this::createEmployee).orElseThrow();
    }

    @Override
    public ru.sber.transport.database.limits.tables.Employee table() {
        return Tables.EMPLOYEE;
    }

    private Employee createEmployee(EmployeeRecord record) {
        return new Employee() {
            @Override
            public UUID id() {
                return record.getId();
            }

            @Override
            public UUID departmentId() {
                return record.getDepartmentId();
            }
        };
    }
}
