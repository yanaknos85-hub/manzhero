package ru.sber.transport.tariff_fleet.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тест маппера должности")
class PositionMapperTest {

    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @Test
    void positionMessageToPosition() {
        var message = PositionMessage.builder()
                .id(UUID.randomUUID())
                .deleted(false)
                .organizationId(UUID.randomUUID())
                .positionName("positionName")
                .selfApproved(false)
                .build();
        var actual = mapper.positionMessageToPosition(message);
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.getOrganizationId(), actual.getOrganizationId());
        assertEquals(message.getPositionName(), actual.getPositionName());
        assertTrue(actual.isActive());
    }
}