package ru.sber.transport.fraud.monitoring.providers.department;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.DepartmentRecord;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsDatabaseProvider;

import java.util.Optional;
import java.util.UUID;


/**
 * Реализация провайдера подразделений
 */
@Slf4j
@RequiredArgsConstructor
public class DepartmentsDatabaseProviderImpl implements DepartmentsDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.Department, DepartmentRecord, UUID> {

    @Override
    public Department createOrUpdate(Department source) {
        if (source == null) {
            return null;
        }
        final var departmentRecord = findById(source.getId()).orElseGet(DepartmentRecord::new);
        departmentRecord.setId(source.getId());
        departmentRecord.setHeadId(source.getHeadId());
        departmentRecord.setParentId(source.getParentId());
        departmentRecord.setName(source.getName());
        departmentRecord.setCode(source.getCode());
        final var saved = save(departmentRecord);

        return createDepartment(saved);
    }

    @Override
    public Department get(UUID id) {
        return findById(id)
                .map(this::createDepartment)
                .orElse(null);
    }

    @Override
    public Optional<UUID> getWithoutName() {
        return context().selectFrom(table())
                .where(table().NAME.isNull()).limit(1)
                .fetchOptional()
                .map(DepartmentRecord::getId);
    }

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.Department table() {
        return Tables.DEPARTMENT;
    }

    private Department createDepartment(@NotNull DepartmentRecord source) {
        return new Department() {
            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public UUID getHeadId() {
                return source.getHeadId();
            }

            @Override
            public UUID getParentId() {
                return source.getParentId();
            }

            @Override
            public String getName() {
                return source.getName();
            }

            @Override
            public String getCode() {
                return source.getCode();
            }
        };
    }
}
