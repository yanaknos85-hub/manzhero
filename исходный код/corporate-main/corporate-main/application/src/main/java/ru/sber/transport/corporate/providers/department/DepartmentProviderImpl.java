package ru.sber.transport.corporate.providers.department;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.providers.DepartmentProvider;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.providers.BaseProvider;
import ru.sber.transport.database.corporate.tables.records.DepartmentRecord;
import ru.sber.transport.corporate.providers.department.mappers.DepartmentDatabaseMapper;

import java.time.OffsetDateTime;
import java.util.*;

@RequiredArgsConstructor
@Transactional
@Repository
@Getter(AccessLevel.PROTECTED)
public class DepartmentProviderImpl extends BaseProvider<Department, DepartmentFilter, DepartmentRecord> implements DepartmentProvider {

    private final DepartmentDatabaseMapper mapper;

    private final HumanReadableProvider<Department> humanReadableProvider;

    @Override
    protected DepartmentRecord createItem() {
        return new DepartmentRecord();
    }

    @Override
    protected SelectConditionStep<Record> filtering(SelectJoinStep<Record> query, Table<DepartmentRecord> table, DepartmentFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<TableField<DepartmentRecord, ?>> minProjection() {
        return List.of(table().ID, table().NAME);
    }

    @Override
    protected TableField<DepartmentRecord, UUID> id() {
        return table().ID;
    }

    @Override
    protected TableField<DepartmentRecord, OffsetDateTime> updateTime() {
        return table().UPDATE_TIME;
    }

    @Override
    public List<UUID> findOfHead(UUID employeeId) {
        return context()
                .select(table().ID)
                .from(table())
                .where(table().HEAD.eq(employeeId))
                .fetchInto(UUID.class);
    }

    @Override
    public Map<UUID, UUID> getOrganizations(List<UUID> departments) {
        return context().select(table().ID, table().ORGANIZATION_ID)
                .from(table())
                .where(table().ID.in(departments))
                .fetchMap(table().ID, table().ORGANIZATION_ID);
    }

    @Override
    public void fillHeads(UUID organizationId) {
        final var recursive = getHeadlessDepartmentsRecursive(organizationId);
        final var recAliasName = DSL.name("rec");
        final var newHeadField = DSL.field(recAliasName.append("new_head"), UUID.class);

        context().update(table())
                .set(table().HEAD, newHeadField)
                .from(recursive.asTable(recAliasName))
                .where(table().ID.eq(DSL.field(recAliasName.append("id"), UUID.class)))
                .and(newHeadField.isNotNull())
                .execute();
    }

    @Override
    public List<UUID> getHeadlessDepartments(UUID organizationId) {
        final var recursive = getHeadlessDepartmentsRecursive(organizationId);
        return recursive.where(DSL.field(DSL.name("head"), UUID.class).isNull()).fetch(DSL.field(DSL.name("id"), UUID.class));
    }

    @Override
    protected TableField<DepartmentRecord, String> humanReadableId() {
        return table().HUMANREADABLEID;
    }

    @Override
    protected TableField<DepartmentRecord, UUID> organizationId() {
        return table().ORGANIZATION_ID;
    }

    @Override
    protected TableField<DepartmentRecord, String> syncId() {
        return table().SYNC_ID;
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Department table() {
        return ru.sber.transport.database.corporate.tables.Department.DEPARTMENT;
    }

    private @NotNull SelectWhereStep<Record> getHeadlessDepartmentsRecursive(UUID organizationId) {
        final var recursiveName = DSL.name("rec_dep");
        final var newHeadFieldName = "new_head";
        final var recursiveIdField = DSL.field(recursiveName.append("id"), UUID.class);

        final var initFields = List.of(
                table().ID,
                table().PARENT_ID,
                table().HEAD,
                table().HEAD.as(newHeadFieldName)
        );
        final var initQuery = context().select(initFields)
                .from(table())
                .where(table().ORGANIZATION_ID.eq(organizationId))
                .and(table().PARENT_ID.isNull());

        final var recursiveFields = List.of(
                table().ID,
                table().PARENT_ID,
                table().HEAD,
                DSL.coalesce(table().HEAD, DSL.field(recursiveName.append(newHeadFieldName), UUID.class)).as(newHeadFieldName)
        );
        final var recursiveQuery = context().select(recursiveFields)
                .from(table())
                .innerJoin(recursiveName)
                .on(recursiveIdField.eq(table().PARENT_ID));

        return context().withRecursive(recursiveName).as(initQuery.union(recursiveQuery)).selectFrom(recursiveName);
    }
}
