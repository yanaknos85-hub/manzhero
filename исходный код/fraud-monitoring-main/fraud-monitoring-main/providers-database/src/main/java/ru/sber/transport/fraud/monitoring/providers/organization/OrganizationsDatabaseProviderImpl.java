package ru.sber.transport.fraud.monitoring.providers.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.OrganizationRecord;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsDatabaseProvider;

import java.util.UUID;


/**
 * Реализация провайдера организаций
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OrganizationsDatabaseProviderImpl implements OrganizationsDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.Organization, OrganizationRecord, UUID> {

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.Organization table() {
        return Tables.ORGANIZATION;
    }

    @Override
    public Organization createOrUpdate(Organization source) {
        final var item = findById(source.getId()).orElseGet(OrganizationRecord::new);
        item.setId(source.getId());
        item.setDigitId((int) source.getDigitId());

        final var saved = save(item);

        return createOrganization(saved);
    }

    @Override
    public Organization get(UUID id) {
        return findById(id).map(this::createOrganization)
                .map(it -> {
                    log.info("Organization {} found", id);
                    return it;
                }).orElse(null);
    }

    @NotNull
    private Organization createOrganization(OrganizationRecord source) {
        return new Organization() {

            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public long getDigitId() {
                return source.getDigitId();
            }
        };
    }
}
