package ru.sber.transport.corporate.providers.organization;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.TableField;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.business.providers.OrganizationProvider;
import ru.sber.transport.database.corporate.Tables;
import ru.sber.transport.database.corporate.tables.records.OrganizationRecord;
import ru.sber.transport.corporate.providers.organization.mappers.OrganizationDatabaseMapper;
import ru.sber.transport.database.corporate.tables.records.TransportOrgRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
@Transactional
class OrganizationProviderImpl implements OrganizationProvider, JooqRepository<ru.sber.transport.database.corporate.tables.Organization, OrganizationRecord, UUID> {

    private final OrganizationDatabaseMapper mapper;

    @Override
    public boolean exists(String name) {
        return isFetchExists(table().OFFICIAL_NAME, name);
    }

    @Override
    public Organization save(Organization source) {
        if (source.getId() == null) {
            source.setId(UUID.randomUUID());
        }
        var saved = save(mapper.toDatabase(source));
        return mapper.toBusiness(saved);
    }

    @Override
    public List<Organization> get() {
        final var transportTypes = context().selectFrom(Tables.TRANSPORT_ORG)
                .fetch()
                .stream()
                .collect(Collectors.toMap(TransportOrgRecord::getOrganizationId, List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));
        return context().selectFrom(table()).fetchInto(table()).stream()
                .map(mapper::toBusiness)
                .map(it -> {
                    it.setAvailableClasses(Optional.ofNullable(transportTypes.get(it.getId())).orElse(List.of()).stream().map(TransportOrgRecord::getTransportType).toList());
                    return it;
                })
                .toList();
    }

    @Override
    public Optional<Organization> get(UUID id) {
        final var transportTypes = context().selectFrom(Tables.TRANSPORT_ORG).where(Tables.TRANSPORT_ORG.ORGANIZATION_ID.eq(id)).fetchInto(TransportOrgRecord.class);
        return findById(id).map(mapper::toBusiness).map(it -> {
            it.setAvailableClasses(transportTypes.stream().map(TransportOrgRecord::getTransportType).toList());
            return it;
        });
    }

    @Override
    public boolean existsSync(String syncId) {
        return isFetchExists(table().SYNC_ID, syncId);
    }

    @Override
    public Optional<Organization> get(@NonNull String organizationId) {
        return context().selectFrom(table())
                .where(table().SYNC_ID.eq(organizationId))
                .fetchOptionalInto(OrganizationRecord.class)
                .map(mapper::toBusiness);
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Organization table() {
        return ru.sber.transport.database.corporate.tables.Organization.ORGANIZATION;
    }

    private boolean isFetchExists(TableField<OrganizationRecord, String> field, String name) {
        return context().fetchExists(context().selectFrom(table()).where(field.eq(name)));
    }
}
