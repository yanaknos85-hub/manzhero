package ru.sber.transport.tariff_fleet.provider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMapper;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера организаций")
class OrganizationProviderImplTest {

    @InjectMocks
    private OrganizationProviderImpl provider;
    @Mock
    private OrganizationService service;
    @Mock
    private OrganizationMapper mapper;
    private Organization organization;
    private OrganizationMessage message;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(UUID.randomUUID())
                .digitId(1L)
                .officialName("officialName")
                .build();
        message = new OrganizationMessage();
        message.setId(organization.getId());
        message.setOfficialName(organization.getOfficialName());
        message.setTid("tid");
        message.setMsrn("msrn");
    }

    @Test
    void delete() {
        var notExistId = UUID.randomUUID();
        var message2 = new OrganizationMessage();
        message2.setId(notExistId);
        message2.setOfficialName("officialName2");
        message2.setTid("tid2");
        message2.setMsrn("msrn2");
        when(service.get(message.getId())).thenReturn(Optional.of(organization));
        when(service.get(notExistId)).thenReturn(Optional.empty());
        doNothing().when(service).delete(organization);
        provider.delete(message);
        provider.delete(message2);
        verify(service).get(message.getId());
        verify(service).get(message2.getId());
        verify(service).delete(organization);
        verify(service).delete(any());
    }

    @Test
    void saveOrUpdate() {
        when(mapper.organizationMessageToOrganization(message)).thenReturn(organization);
        doNothing().when(service).saveOrUpdate(organization);
        provider.save(message);
        verify(mapper).organizationMessageToOrganization(message);
        verify(service).saveOrUpdate(organization);
    }
}