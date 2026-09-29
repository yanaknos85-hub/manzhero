package ru.sber.transport.tariff_fleet.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.PositionRepository;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Position;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.sber.transport.tariff_fleet.TestData.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с должностями")
class PositionServiceImplTest {
    @InjectMocks
    private PositionServiceImpl service;
    @Mock
    private PositionRepository repository;
    private Position position;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = createOrganization1();
        position = Position.builder()
                .id(POSITION_1_ID)
                .organizationId(UUID.randomUUID())
                .positionName("positionName")
                .build();
    }

    @Test
    void get() {
        when(repository.findById(position.getId())).thenReturn(Optional.of(position));
        assertEquals(service.get(position.getId()), Optional.of(position));
        assertEquals(service.get(UUID.randomUUID()), Optional.empty());
    }

    @Test
    void delete() {
        when(repository.save(position)).thenReturn(position);
        service.delete(position);
        verify(repository).save(position);
    }
    
    @Test
    void saveOrUpdate() {
        var updatePosition1 = Position.builder()
                                      .id(POSITION_1_ID)
                                      .positionName("positionName1Updated")
                                      .organizationId(ORGANIZATION_2_ID)
                                      .build();
        var position2 = createPosition2(organization);
        when(repository.findById(position.getId())).thenReturn(Optional.of(position));
        when(repository.findById(position2.getId())).thenReturn(Optional.empty());
        when(repository.save(updatePosition1)).thenReturn(updatePosition1);
        when(repository.save(position2)).thenReturn(position2);
        service.saveOrUpdate(updatePosition1);
        service.saveOrUpdate(position2);
        verify(repository).findById(position.getId());
        verify(repository).findById(position2.getId());
        verify(repository).save(updatePosition1);
        verify(repository).save(position2);
    }
}