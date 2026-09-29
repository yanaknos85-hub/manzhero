package ru.sber.transport.fraud.monitoring.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.fraud.monitoring.business.OrganizationsService;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsGrpcProvider;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OrganizationsServiceImpl implements OrganizationsService {

    private final OrganizationsDatabaseProvider organizationsDatabaseProvider;
    private final OrganizationsGrpcProvider organizationsGrpcProvider;

    @Override
    public Organization createOrUpdate(Organization source) {
        return organizationsDatabaseProvider.createOrUpdate(source);
    }

    @Override
    public Organization getExistedOrCreate(UUID id) {
        final var organization = organizationsDatabaseProvider.get(id);
        if (organization != null) {
            return organization;
        }

        final var requestedOrganization = organizationsGrpcProvider.get(id);
        final var saved = createOrUpdate(requestedOrganization);
        log.info("Responded organization {} saved", id);
        return saved;
    }
}
