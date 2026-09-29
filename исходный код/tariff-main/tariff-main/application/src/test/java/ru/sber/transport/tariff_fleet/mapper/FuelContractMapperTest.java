package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;
import ru.sber.transport.tariff_fleet.database.projection.GetFuelContractProjection;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractGetAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractGetSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractPostAllOrganizationsDto;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class FuelContractMapperTest {
    
    private final FuelContractMapper fuelContractMapper = Mappers.getMapper(FuelContractMapper.class);
    
    @Test
    void fuelContractPostDtoToFuelContract() {
        var contractDto = Instancio.create(FuelContractPostAllOrganizationsDto.class);
        var contract = Instancio.create(Contract.class);
        var expected = new FuelContract(
                                        contract,
                                        contractDto.getContractorId(),
                                        null,
                                        contractDto.getName(),
                                        contractDto.getAmountWithVat(),
                                        contractDto.getAmountWithoutVat(),
                                        Organization.builder()
                                                    .id(contractDto.getOrganizationId())
                                                    .build(),
                                        contractDto.getLogoS3Id(),
                                        Collections.emptyList());
        expected.setServicePoints(contractDto.getServicePoints().stream()
                                             .map(serviceStationDto -> new ServicePoint(null,
                                                                                        serviceStationDto.address(),
                                                                                        serviceStationDto.latitude(),
                                                                                        serviceStationDto.longitude(),
                                                                                        null,
                                                                                        true))
                                             .toList());
        var actual = fuelContractMapper.fuelContractPostDtoToFuelContract(contractDto, contract);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(fuelContractMapper.fuelContractPostSelfOrganizationDtoToFuelContract(null, null, null)).isNull();
    }
    
    @Test
    void getFuelContractProjectionToFuelContractGetAllOrganizationsDto() {
        var fuelContractGetAllOrganizationsDto = Instancio.create(FuelContractGetAllOrganizationsDto.class);
        var getFuelContractProjection = createGetFuelContractAllOrganizationsProjection(fuelContractGetAllOrganizationsDto);
        var expected = new FuelContractGetAllOrganizationsDto(getFuelContractProjection.getId(),
                                                              DocumentType.FUEL,
                                                              getFuelContractProjection.getNumber(),
                                                              getFuelContractProjection.getStart(),
                                                              getFuelContractProjection.getEnd(),
                                                              getFuelContractProjection.getActive(),
                                                              getFuelContractProjection.getContractorName(),
                                                              getFuelContractProjection.getOrganizationName(),
                                                              getFuelContractProjection.getAmount());
        var actual = fuelContractMapper.getFuelContractProjectionToFuelContractGetAllOrganizationsDto(getFuelContractProjection);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void getFuelContractProjectionToFuelContractGetSelfOrganizationDto() {
        var fuelContractGetSelfOrganizationDto = Instancio.create(FuelContractGetSelfOrganizationDto.class);
        var getFuelContractProjection = createGetFuelContractAllOrganizationsProjection(fuelContractGetSelfOrganizationDto);
        var expected = new FuelContractGetSelfOrganizationDto(getFuelContractProjection.getId(),
                                                              DocumentType.FUEL,
                                                              getFuelContractProjection.getNumber(),
                                                              getFuelContractProjection.getStart(),
                                                              getFuelContractProjection.getEnd(),
                                                              getFuelContractProjection.getActive(),
                                                              getFuelContractProjection.getContractorName(),
                                                              getFuelContractProjection.getOrganizationName(),
                                                              getFuelContractProjection.getAmount());
        var actual = fuelContractMapper.getFuelContractProjectionToFuelContractGetSelfOrganizationDto(getFuelContractProjection);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void toFuelContractMessage() {
        var expected = Instancio.create(FuelContract.class);
        
        var actual = fuelContractMapper.toFuelContractMessage(expected);
        assertThat(actual.contractId()).isEqualTo(expected.getContractId());
        assertThat(actual.contractorId()).isEqualTo(expected.getContractorId());
        assertThat(actual.logoS3Id()).isEqualTo(expected.getLogoS3Id());
        assertThat(actual.servicePointsName()).isEqualTo(expected.getServicePointsName());
        assertThat(actual.organizationId()).isEqualTo(expected.getOrganization().getId());
        assertThat(actual.number()).isEqualTo(expected.getContract().getNumber());
        assertThat(actual.start()).isEqualTo(expected.getContract().getStart());
        assertThat(actual.end()).isEqualTo(expected.getContract().getEnd());
        assertThat(actual.active()).isEqualTo(expected.getContract().isActive());
        assertThat(actual.servicePoints()).hasSize(expected.getServicePoints().size());
        var servicePoint = actual.servicePoints().get(0);
        assertThat(servicePoint.active()).isEqualTo(expected.getServicePoints().get(0).isActive());
        assertThat(servicePoint.address()).isEqualTo(expected.getServicePoints().get(0).getAddress());
    }
    
    GetFuelContractProjection createGetFuelContractAllOrganizationsProjection(FuelContractGetAllOrganizationsDto fuelGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetFuelContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(fuelGetContractDto.getContractorName());
        projection.setId(fuelGetContractDto.getId());
        projection.setNumber(fuelGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(fuelGetContractDto.getAmountWithoutVat());
        projection.setStart(fuelGetContractDto.getStart());
        projection.setEnd(fuelGetContractDto.getEnd());
        projection.setActive(fuelGetContractDto.isActive());
        return projection;
    }
    
    GetFuelContractProjection createGetFuelContractAllOrganizationsProjection(FuelContractGetSelfOrganizationDto fuelGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetFuelContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(fuelGetContractDto.getContractorName());
        projection.setId(fuelGetContractDto.getId());
        projection.setNumber(fuelGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(fuelGetContractDto.getAmountWithoutVat());
        projection.setStart(fuelGetContractDto.getStart());
        projection.setEnd(fuelGetContractDto.getEnd());
        projection.setActive(fuelGetContractDto.isActive());
        return projection;
    }
    
}