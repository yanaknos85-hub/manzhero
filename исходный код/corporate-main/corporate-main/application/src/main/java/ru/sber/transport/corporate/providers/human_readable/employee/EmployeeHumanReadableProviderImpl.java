package ru.sber.transport.corporate.providers.human_readable.employee;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.providers.human_readable.BaseHumanReadableProvider;

import static ru.sber.transport.database.corporate.Tables.EMPLOYEE;

@Transactional
@RequiredArgsConstructor
@Repository
class EmployeeHumanReadableProviderImpl extends BaseHumanReadableProvider<ru.sber.transport.database.corporate.tables.Employee, Employee> {


    @Override
    protected String prefix() {
        return "US";
    }

    @Override
    protected ru.sber.transport.database.corporate.tables.Employee entityTable() {
        return EMPLOYEE;
    }

    @Override
    protected Field<String> humanReadableField(ru.sber.transport.database.corporate.tables.Employee table) {
        return table.HUMANREADABLEID;
    }

    @Override
    public Class<Employee> entityClass() {
        return Employee.class;
    }
}
