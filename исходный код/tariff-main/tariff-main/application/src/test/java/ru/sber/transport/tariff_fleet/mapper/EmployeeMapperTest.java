package ru.sber.transport.tariff_fleet.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тест маппера сотрудника")
class EmployeeMapperTest {

    private final EmployeeMapper mapper = Mappers.getMapper(EmployeeMapper.class);

    @Test
    void employeeMessageToEmployee() {
        var message = EmployeeMessage.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .firstName("firstName")
                .lastName("lastName")
                .patronymic("patronymic")
                .positionId(UUID.randomUUID())
                .deleted(false)
                .humanReadableId("humanReadableId")
                .organizationId(UUID.randomUUID())
                .availableTransportTypes(Collections.singleton("SPECIAL"))
                .departmentId(UUID.randomUUID())
                .mobilePhone("mobilePhone")
                .personnelNumber("personnelNumber")
                .organizationId(UUID.randomUUID())
                .costCenter("5200L99780")
                .build();
        var actual = mapper.employeeMessageToEmployee(message);
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(message.getFirstName(), actual.getFirstName());
        assertEquals(message.getLastName(), actual.getLastName());
        assertEquals(message.getPatronymic(), actual.getPatronymic());
        assertEquals(message.getUserId(), actual.getUserId());
        assertEquals(message.getPersonnelNumber(), actual.getPersonnelNumber());
        assertEquals(message.getDepartmentId(), actual.getDepartment().getId());
        assertEquals(message.getPositionId(), actual.getPosition().getId());
        assertEquals(message.getMobilePhone(), actual.getMobilePhone());
        assertTrue(actual.isActive());
        assertEquals(message.getOrganizationId(), actual.getOrganization().getId());
        assertEquals(message.getCostCenter(), actual.getCostCenter());
    }
}