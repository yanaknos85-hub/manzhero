package ru.sber.transport.fraud.monitoring.providers.employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.EmployeeRecord;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sber.transport.fraud.monitoring.providers.EmployeesDatabaseProvider;

import java.util.UUID;

/**
 * Реализация провайдера сотрудников
 */
@Slf4j
@RequiredArgsConstructor
public class EmployeeDatabaseProviderImpl implements EmployeesDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.Employee, EmployeeRecord, UUID> {

    @Override
    public Employee createOrUpdate(Employee source) {
        if (source == null) {
            return null;
        }

        var saved = context().insertInto(table())
                .set(table().ID, source.getId())
                .set(table().DEPARTMENT_ID, source.getDepartmentId())
                .set(table().ORGANIZATION_ID, source.getOrganizationId())
                .set(table().LAST_NAME, source.getLastName())
                .set(table().FIRST_NAME, source.getFirstName())
                .set(table().PATRONYMIC, source.getPatronymic())
                .set(table().POSITION_ID, source.getPositionId())
                .set(table().PERSONNEL_NUMBER, source.getPersonnelNumber())
                .set(table().COST_CENTER, source.getCostCenter())
                .onConflict(table().ID)
                .doUpdate()
                .set(table().DEPARTMENT_ID, DSL.excluded(table().DEPARTMENT_ID))
                .set(table().ORGANIZATION_ID, DSL.excluded(table().ORGANIZATION_ID))
                .set(table().LAST_NAME, DSL.excluded(table().LAST_NAME))
                .set(table().FIRST_NAME, DSL.excluded(table().FIRST_NAME))
                .set(table().PATRONYMIC, DSL.excluded(table().PATRONYMIC))
                .set(table().POSITION_ID, DSL.excluded(table().POSITION_ID))
                .set(table().PERSONNEL_NUMBER, DSL.excluded(table().PERSONNEL_NUMBER))
                .set(table().COST_CENTER, DSL.excluded(table().COST_CENTER))
                .returning()
                .fetchOne();

        if (saved == null) {
            saved = getById(source.getId());
        }

        return createEmployee(saved);
    }

    @Override
    public Employee get(UUID id) {
        return findById(id)
                .map(this::createEmployee)
                .map(it -> {
                    log.debug("Employee {} found", id);
                    return it;
                }).orElse(null);
    }

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.Employee table() {
        return Tables.EMPLOYEE;
    }

    private Employee createEmployee(@NotNull EmployeeRecord employeeRecord) {
        return new Employee() {

            @Override
            public UUID getId() {
                return employeeRecord.getId();
            }

            @Override
            public UUID getDepartmentId() {
                return employeeRecord.getDepartmentId();
            }

            @Override
            public UUID getOrganizationId() {
                return employeeRecord.getOrganizationId();
            }

            @Override
            public String getLastName() {
                return employeeRecord.getLastName();
            }

            @Override
            public String getFirstName() {
                return employeeRecord.getFirstName();
            }

            @Override
            public String getPatronymic() {
                return employeeRecord.getPatronymic();
            }

            @Override
            public UUID getPositionId() {
                return employeeRecord.getPositionId();
            }

            @Override
            public String getPersonnelNumber() {
                return employeeRecord.getPersonnelNumber();
            }

            @Override
            public String getCostCenter() {
                return employeeRecord.getCostCenter();
            }
        };
    }
}
