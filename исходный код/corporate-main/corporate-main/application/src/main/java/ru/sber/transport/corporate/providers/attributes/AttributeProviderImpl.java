package ru.sber.transport.corporate.providers.attributes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.Attribute;
import ru.sber.transport.corporate.business.providers.AttributeProvider;
import ru.sber.transport.database.corporate.tables.records.AttributeRecord;
import ru.sber.transport.corporate.providers.attributes.mappers.AttributeDatabaseMapper;

import java.util.List;
import java.util.UUID;

import static ru.sber.transport.database.corporate.tables.Attribute.ATTRIBUTE;
import static ru.sber.transport.database.corporate.tables.EmployeeAttribute.EMPLOYEE_ATTRIBUTE;

@Repository
@RequiredArgsConstructor
class AttributeProviderImpl implements AttributeProvider, JooqRepository<ru.sber.transport.database.corporate.tables.Attribute, AttributeRecord, UUID> {

    private final AttributeDatabaseMapper mapper;

    @Override
    public List<Attribute> findOfEmployee(UUID employeeId) {
        return context().select(table().fields())
                .from(table())
                .innerJoin(EMPLOYEE_ATTRIBUTE).on(EMPLOYEE_ATTRIBUTE.ATTRIBUTE_ID.eq(table().ID))
                .where(EMPLOYEE_ATTRIBUTE.EMPLOYEE_ID.eq(employeeId))
                .fetchInto(table())
                .stream()
                .map(mapper::toBusiness)
                .toList();
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Attribute table() {
        return ATTRIBUTE;
    }
}
