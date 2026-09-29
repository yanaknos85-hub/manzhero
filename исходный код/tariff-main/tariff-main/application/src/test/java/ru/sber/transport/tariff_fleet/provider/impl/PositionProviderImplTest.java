package ru.sber.transport.tariff_fleet.provider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Position;
import ru.sber.transport.tariff_fleet.exception.AwaitingSynchronizationException;
import ru.sber.transport.tariff_fleet.mapper.PositionMapper;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.provider.impl.PositionProviderImpl.ERROR_NOT_IN_DB_MESSAGE_FORMAT;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка провайдера должностей")
class PositionProviderImplTest {
    
    @InjectMocks
    private PositionProviderImpl provider;
    @Mock
    private PositionService service;
    @Mock
    private PositionMapper mapper;
    @Mock
    private OrganizationService organizationService;
    private Organization organization;
    private Position position;
    private PositionMessage message;
    
    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                                   .id(UUID.randomUUID())
                                   .officialName("officialName")
                                   .build();
        position = Position.builder()
                           .id(UUID.randomUUID())
                           .organizationId(organization.getId())
                           .positionName("positionName")
                           .build();
        message = PositionMessage.builder()
                                 .id(position.getId())
                                 .positionName(position.getPositionName())
                                 .organizationId(organization.getId())
                                 .selfApproved(true)
                                 .build();
    }
    
    @Test
    void delete() {
        when(service.get(message.getId())).thenReturn(Optional.of(position));
        doNothing().when(service).delete(position);
        provider.delete(message);
        verify(service).get(message.getId());
        verify(service).delete(position);
    }
    
    @Test
    void saveOrUpdate() {
        when(mapper.positionMessageToPosition(message)).thenReturn(position);
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.of(organization));
        doNothing().when(service).saveOrUpdate(position);
        provider.save(message);
        verify(mapper).positionMessageToPosition(message);
        verify(service).saveOrUpdate(position);
        verify(organizationService).get(message.getOrganizationId());
    }
    
    @Test
    void saveNoOrganizationInDatabase() {
        when(organizationService.get(message.getOrganizationId())).thenReturn(Optional.empty());
        var exception = assertThrows(AwaitingSynchronizationException.class, () -> provider
                .save(message));
        assertEquals(String.format(ERROR_NOT_IN_DB_MESSAGE_FORMAT,
                                   message.getId(),
                                   message.getPositionName(),
                                   message.getOrganizationId(),
                                   "organization"), exception.getMessage());
        verify(mapper, never()).positionMessageToPosition(message);
        verify(service, never()).saveOrUpdate(position);
        verify(organizationService).get(message.getOrganizationId());
    }
}