package ru.sber.transport.corporate.providers.human_readable.department;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.providers.human_readable.BaseHumanReadableProvider;

import static ru.sber.transport.database.corporate.Tables.DEPARTMENT;

@Transactional
@RequiredArgsConstructor
@Repository
class DepartmentHumanReadableProviderImpl extends BaseHumanReadableProvider<ru.sber.transport.database.corporate.tables.Department, Department> {

    @Override
    protected String prefix() {
        return "DT";
    }

    @Override
    protected ru.sber.transport.database.corporate.tables.Department entityTable() {
        return DEPARTMENT;
    }

    @Override
    protected Field<String> humanReadableField(ru.sber.transport.database.corporate.tables.Department table) {
        return table.HUMANREADABLEID;
    }

    @Override
    public Class<Department> entityClass() {
        return Department.class;
    }
}
