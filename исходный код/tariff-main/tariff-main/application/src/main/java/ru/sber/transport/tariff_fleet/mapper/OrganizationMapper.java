package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.OrganizationNameWithDepartmentInfo;
import ru.sber.transport.tariff_fleet.dto.DepartmentInfoDto;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

/**
 * Маппер организаций.
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    
    Organization organizationMessageToOrganization(OrganizationMessage source);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "officialName")
    GetAllActiveOrganizationNamesDto organizationToGetAllActiveOrganizationNamesDto(Organization source);
    
    @Mapping(target = "id", source = "departmentId")
    DepartmentInfoDto organizationWithDepartmentIntoDto(OrganizationNameWithDepartmentInfo model);
}
