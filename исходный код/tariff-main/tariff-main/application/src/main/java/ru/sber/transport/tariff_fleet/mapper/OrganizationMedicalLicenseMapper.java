package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.OrganizationMedicalLicense;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePostDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;

/**
 * Мэппер медицинских лицензий организаций
 */
@Mapper(componentModel = "spring")
public interface OrganizationMedicalLicenseMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "series", source = "source.series")
    @Mapping(target = "number", source = "source.number")
    @Mapping(target = "issueDate", source = "source.issueDate")
    @Mapping(target = "expiryDate", source = "source.expiryDate")
    OrganizationMedicalLicense organizationMedicalLicenseDtoToOrganizationMedicalLicense(OrganizationMedicalLicensePostDto source);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "series", source = "series")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "expiryDate", source = "expiryDate")
    OrganizationMedicalLicenseMessage organizationMedicalLicenseToOrganizationMedicalLicenseMessage(OrganizationMedicalLicense source);
}