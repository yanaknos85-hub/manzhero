package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.database.model.OrganizationMedicalLicense;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePostDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationMedicalLicenseMapperTest {
    
    private final OrganizationMedicalLicenseMapper organizationMedicalLicenseMapper = Mappers.getMapper(OrganizationMedicalLicenseMapper.class);
    
    @Test
    void organizationMedicalLicenseDtoToOrganizationMedicalLicense() {
        var source = Instancio.create(OrganizationMedicalLicensePostDto.class);
        var expected = new OrganizationMedicalLicense(null,
                                                      source.series(),
                                                      source.number(),
                                                      source.issueDate(),
                                                      source.expiryDate());
        var actual = organizationMedicalLicenseMapper.organizationMedicalLicenseDtoToOrganizationMedicalLicense(source);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(organizationMedicalLicenseMapper.organizationMedicalLicenseToOrganizationMedicalLicenseMessage(null)).isNull();
    }
    
    @Test
    void organizationMedicalLicenseToOrganizationMedicalLicenseMessage() {
        var source = Instancio.create(OrganizationMedicalLicense.class);
        var expected = new OrganizationMedicalLicenseMessage(source.getId(),
                                                             source.getSeries(),
                                                             source.getNumber(),
                                                             source.getIssueDate(),
                                                             source.getExpiryDate());
        var actual = organizationMedicalLicenseMapper.organizationMedicalLicenseToOrganizationMedicalLicenseMessage(source);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(organizationMedicalLicenseMapper.organizationMedicalLicenseToOrganizationMedicalLicenseMessage(null)).isNull();
    }
}
