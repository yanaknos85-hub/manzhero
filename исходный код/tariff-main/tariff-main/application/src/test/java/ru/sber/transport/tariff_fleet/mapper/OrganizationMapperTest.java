package ru.sber.transport.tariff_fleet.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.database.model.OrganizationNameWithDepartmentInfo;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.tariff_fleet.TestData.createOrganization1;

@DisplayName("Тест маппера организации")
class OrganizationMapperTest {
    
    private final OrganizationMapper mapper = Mappers.getMapper(OrganizationMapper.class);
    
    @Test
    void organizationMessageToOrganization() {
        var message = new OrganizationMessage();
        message.setId(UUID.randomUUID());
        message.setDeleted(false);
        message.setAddress("address");
        message.setContacts(Collections.emptyList());
        message.setDigitId(11L);
        message.setMsrn("msrn");
        message.setOfficialName("officialName");
        message.setTid("tid");
        var actual = mapper.organizationMessageToOrganization(message);
        assertEquals(message.getId(), actual.getId());
        assertEquals(message.getDigitId(), actual.getDigitId());
        assertEquals(message.getOfficialName(), actual.getOfficialName());
        assertTrue(actual.isActive());
    }
    
    @Test
    void organizationToGetAllActiveOrganizationNamesDto() {
        var organization = createOrganization1();
        var actual = mapper.organizationToGetAllActiveOrganizationNamesDto(organization);
        assertEquals(organization.getId(), actual.id());
        assertEquals(organization.getOfficialName(), actual.name());
    }
    
    @Test
    void organizationWithDepartmentIntoDto() {
        var model = new OrganizationNameWithDepartmentInfo(
                UUID.randomUUID(),
                "organization",
                UUID.randomUUID(),
                "department",
                UUID.randomUUID()
        );
        var actual = mapper.organizationWithDepartmentIntoDto(model);
        assertEquals(model.departmentId(), actual.id());
        assertEquals(model.departmentName(), actual.departmentName());
        assertEquals(model.parentId(), actual.parentId());
    }
}