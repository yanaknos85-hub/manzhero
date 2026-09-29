package ru.sber.transport.corporate.providers.employee;

import static org.jooq.impl.DSL.noCondition;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.corporate.business.model.EmployeeFilter;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.providers.BaseProvider;
import ru.sber.transport.corporate.providers.employee.mappers.EmployeeDatabaseMapper;
import ru.sber.transport.database.corporate.Tables;
import ru.sber.transport.database.corporate.enums.StructureType;
import ru.sber.transport.database.corporate.tables.records.EmployeeRecord;

@RequiredArgsConstructor
@Repository
@Transactional
@Getter(AccessLevel.PROTECTED)
class EmployeeProviderImpl extends BaseProvider<Employee, EmployeeFilter, EmployeeRecord> implements EmployeeProvider {

    private static final String LIKE_FORMAT = "%%%s%%";

    private final EmployeeDatabaseMapper mapper;

    private final HumanReadableProvider<Employee> humanReadableProvider;

    @Override
    protected EmployeeRecord createItem() {
        return new EmployeeRecord();
    }

    @Override
    protected TableField<EmployeeRecord, UUID> id() {
        return table().ID;
    }

    @Override
    protected TableField<EmployeeRecord, OffsetDateTime> updateTime() {
        return table().UPDATE_TIME;
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Employee table() {
        return ru.sber.transport.database.corporate.tables.Employee.EMPLOYEE;
    }

    @Override
    public List<Employee> get(Collection<Map.Entry<UUID, String>> source) {
        var sql = context()
            .selectFrom(table())
            .where(DSL.concat(table().ORGANIZATION_ID, table().PERSONNEL_NUMBER)
                .in(source.parallelStream().map(it -> it.getKey().toString() + it.getValue()).toList()));
        return sql.fetchInto(EmployeeRecord.class)
            .parallelStream()
            .map(mapper::toBusiness)
            .toList();
    }

    @Override
    public Optional<Employee> get(UUID organizationId, String headId) {
        return context().selectFrom(table())
            .where(table().ORGANIZATION_ID.eq(organizationId).and(table().PERSONNEL_NUMBER.eq(headId)))
            .fetchOptionalInto(EmployeeRecord.class)
            .map(mapper::toBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> getByIdOrPersonalNumber(UUID id, String personalNumber) {
        var condition = noCondition();

        condition = condition.and(Optional.ofNullable(id)
            .map(Tables.EMPLOYEE.ID::eq)
            .orElse(noCondition()));

        if (StringUtils.isNotBlank(personalNumber)) {
            condition = condition.and(Tables.EMPLOYEE.PERSONNEL_NUMBER.eq(personalNumber));
        }

        return context()
            .selectFrom(Tables.EMPLOYEE)
            .where(condition)
            .fetchInto(EmployeeRecord.class)
            .parallelStream()
            .map(mapper::toBusiness)
            .reduce((a, b) -> {
                throw new UnsupportedOperationException("Найдено более одного пользователя по входящим параметрам");
            });
    }

    @Override
    protected TableField<EmployeeRecord, String> humanReadableId() {
        return table().HUMANREADABLEID;
    }

    @Override
    protected TableField<EmployeeRecord, UUID> organizationId() {
        return table().ORGANIZATION_ID;
    }

    @Override
    protected TableField<EmployeeRecord, String> syncId() {
        return table().PERSONNEL_NUMBER;
    }

    @Override
    protected List<TableField<EmployeeRecord, ?>> minProjection() {
        return List.of(table().ID, table().FIRST_NAME, table().LAST_NAME, table().PATRONYMIC, table().PERSONNEL_NUMBER);
    }

    @Override
    protected SelectConditionStep<Record> filtering(SelectJoinStep<Record> query, Table<EmployeeRecord> table,
        EmployeeFilter filter) {
        var filtered = query.where(DSL.trueCondition());

        final var email = filter.getEmail();
        final var status = filter.getStatus();
        final var fullName = filter.getFullName();
        final var mobilePhone = filter.getMobilePhone();
        final var personnelNumber = filter.getPersonnelNumber();
        final var humanReadableId = filter.getHumanReadableId();
        final var employees = filter.getEmployees();
        final var organizations = filter.getOrganizations();
        final var departments = filter.getDepartments();
        final var orgStructureType = filter.getOrgStructureType();

        if (CollectionUtils.isNotEmpty(organizations)) {
            filtered = filtered.and(table().ORGANIZATION_ID.in(organizations));
        }
        if (CollectionUtils.isNotEmpty(departments)) {
            filtered = filtered.and(table().DEPARTMENT_ID.in(filter.getDepartments()));
        }
        if (CollectionUtils.isNotEmpty(employees)) {
            filtered = filtered.and(table().ID.in(filter.getEmployees()));
        }
        if (email != null) {
            filtered = filtered.and(table().EMAIL.likeIgnoreCase(LIKE_FORMAT.formatted(email)));
        }
        if (mobilePhone != null) {
            filtered = filtered.and(table().MOBILE_PHONE.likeIgnoreCase(LIKE_FORMAT.formatted(mobilePhone)));
        }
        if (personnelNumber != null) {
            filtered = filtered.and(table().PERSONNEL_NUMBER.likeIgnoreCase(LIKE_FORMAT.formatted(personnelNumber)));
        }
        if (humanReadableId != null) {
            filtered = filtered.and(table().HUMANREADABLEID.likeIgnoreCase(LIKE_FORMAT.formatted(humanReadableId)));
        }
        if (fullName != null) {
            final var formatted = fullName.replaceAll("[.\\s]", "").toLowerCase();
            filtered = filtered.and(DSL.or(
                DSL.lower(
                    DSL.replace(DSL.concat(table().LAST_NAME, table().FIRST_NAME, DSL.coalesce(table().PATRONYMIC)),
                        " ", "")).likeIgnoreCase(LIKE_FORMAT.formatted(formatted)),
                DSL.lower(
                    DSL.replace(DSL.concat(table().FIRST_NAME, DSL.coalesce(table().PATRONYMIC), table().LAST_NAME),
                        " ", "")).likeIgnoreCase(LIKE_FORMAT.formatted(formatted))
            ));
        }
        if (status != null) {
            filtered = filtered.and(table().STATUS.cast(String.class).eq(status.name()));
        }
        if (orgStructureType != null) {
            filtered = filtered.and(table().ORG_STRUCTURE_TYPE.eq(StructureType.valueOf(orgStructureType.getValue())));
        }
        return filtered;
    }

}
