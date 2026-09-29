package ru.sber.transport.tariff_fleet.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMapper;
import ru.sber.transport.tariff_fleet.provider.OrganizationProvider;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Реализация провайдера организаций.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class OrganizationProviderImpl implements OrganizationProvider {

    private final OrganizationService service;
    private final OrganizationMapper mapper;

    @Override
    public void delete(OrganizationMessage message) {
        service.get(message.getId()).ifPresent(service::delete);
    }

    @Override
    public void save(OrganizationMessage message) {
        service.saveOrUpdate(mapper.organizationMessageToOrganization(message));
    }
}
