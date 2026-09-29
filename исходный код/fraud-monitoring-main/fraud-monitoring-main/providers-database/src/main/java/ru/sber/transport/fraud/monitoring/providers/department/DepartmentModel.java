package ru.sber.transport.fraud.monitoring.providers.department;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.DepartmentRecord;
import ru.sber.transport.fraud.monitoring.model.Department;

/**
 * Реализация модели департамента
 */
@RequiredArgsConstructor
public class DepartmentModel implements Department {

    @Delegate
    private final DepartmentRecord source;
}