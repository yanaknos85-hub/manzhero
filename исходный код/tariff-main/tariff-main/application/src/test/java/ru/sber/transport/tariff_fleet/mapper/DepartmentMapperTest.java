package ru.sber.transport.tariff_fleet.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест маппера подразделения")
class DepartmentMapperTest {

    private final DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);

    @Test
    void departmentMessageToDepartment() {
        var message = DepartmentMessage.builder()
                .id(UUID.randomUUID())
                .code("code")
                .deleted(false)
                .departmentHeadId(UUID.randomUUID())
                .departmentName("departmentName")
                .humanReadableId("humanReadableId")
                .location("location")
                .organizationId(UUID.randomUUID())
                .parentId(UUID.randomUUID())
                .build();
        var actual = mapper.departmentMessageToDepartment(message);
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.getOrganizationId(), actual.getOrganizationId());
        assertEquals(message.getParentId(), actual.getParentId());
        assertEquals(message.getDepartmentName(), actual.getDepartmentName());
        assertNull(actual.getEasupId());
        assertTrue(actual.isActive());
    }
}