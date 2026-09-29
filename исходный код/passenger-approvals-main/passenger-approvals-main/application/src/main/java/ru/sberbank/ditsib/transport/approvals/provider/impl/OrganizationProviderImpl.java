package ru.sberbank.ditsib.transport.approvals.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.approvals.mappers.OrganizationMapper;
import ru.sberbank.ditsib.transport.approvals.provider.OrganizationProvider;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Реализация провайдера организаций.
 */
@Transactional
@RequiredArgsConstructor
@Component
public class OrganizationProviderImpl implements OrganizationProvider {

    private final OrganizationService organizationService;
    private final OrganizationMapper mapper;

    @Override
    public void delete(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.delete(entity);
    }

    @Override
    public void save(OrganizationMessage message) {
        var entity = mapper.organizationMessageToOrganization(message);
        organizationService.save(entity);
    }
}
