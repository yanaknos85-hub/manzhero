package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;
import ru.sber.transport.tariff_fleet.database.projection.GetRepairContractProjection;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractPostAllOrganizationsDto;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RepairContractMapperTest {
    
    private final RepairContractMapper repairContractMapper = Mappers.getMapper(RepairContractMapper.class);
    
    @Test
    void repairContractPostDtoToRepairContract() {
        var contractDto = Instancio.create(RepairContractPostAllOrganizationsDto.class);
        var contract = Instancio.create(Contract.class);
        var expected = new RepairContract(
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
                                                                                null, true))
                                             .toList());
        var actual = repairContractMapper.repairContractPostDtoToRepairContract(contractDto, contract);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        assertThat(repairContractMapper.repairContractPostSelfOrganizationDtoToRepairContract(null, null, null)).isNull();
    }
    
    @Test
    void getRepairContractProjectionToRepairGetContractDto() {
        var repairGetContractDto = Instancio.create(RepairContractGetDto.class);
        var getRepairContractProjection = createGetRepairContractProjection(repairGetContractDto);
        var expected = new RepairContractGetDto(getRepairContractProjection.getId(),
                                                getRepairContractProjection.getNumber(),
                                                getRepairContractProjection.getStart(),
                                                getRepairContractProjection.getEnd(),
                                                getRepairContractProjection.getActive(),
                                                getRepairContractProjection.getContractorId(),
                                                getRepairContractProjection.getContractorName(),
                                                getRepairContractProjection.getUvhd(),
                                                getRepairContractProjection.getAmount());
        var actual = repairContractMapper.getRepairContractProjectionToRepairGetContractDto(getRepairContractProjection);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void getRepairContractProjectionToRepairContractGetAllOrganizationsDto() {
        var repairContractGetAllOrganizationsDto = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var getRepairContractProjection = createGetRepairContractAllOrganizationsProjection(repairContractGetAllOrganizationsDto);
        var expected = new RepairContractGetAllOrganizationsDto(getRepairContractProjection.getId(),
                                                                DocumentType.REPAIR_AND_MAINTENANCE,
                                                                getRepairContractProjection.getNumber(),
                                                                getRepairContractProjection.getStart(),
                                                                getRepairContractProjection.getEnd(),
                                                                getRepairContractProjection.getActive(),
                                                                getRepairContractProjection.getContractorName(),
                                                                getRepairContractProjection.getOrganizationName(),
                                                                getRepairContractProjection.getAmount());
        var actual = repairContractMapper.getRepairContractProjectionToRepairContractGetAllOrganizationsDto(getRepairContractProjection);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void getRepairContractProjectionToRepairContractGetSelfOrganizationDto() {
        var repairContractGetSelfOrganizationDto = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var getRepairContractProjection = createGetRepairContractAllOrganizationsProjection(repairContractGetSelfOrganizationDto);
        var expected = new RepairContractGetSelfOrganizationDto(getRepairContractProjection.getId(),
                                                                DocumentType.REPAIR_AND_MAINTENANCE,
                                                                getRepairContractProjection.getNumber(),
                                                                getRepairContractProjection.getStart(),
                                                                getRepairContractProjection.getEnd(),
                                                                getRepairContractProjection.getActive(),
                                                                getRepairContractProjection.getContractorName(),
                                                                getRepairContractProjection.getOrganizationName(),
                                                                getRepairContractProjection.getAmount());
        var actual = repairContractMapper.getRepairContractProjectionToRepairContractGetSelfOrganizationDto(getRepairContractProjection);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    GetRepairContractProjection createGetRepairContractProjection(RepairContractGetDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(repairGetContractDto.getContractorId());
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(repairGetContractDto.getUvhd());
        projection.setAmount(repairGetContractDto.getAmount());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }
    
    GetRepairContractProjection createGetRepairContractAllOrganizationsProjection(RepairContractGetAllOrganizationsDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(repairGetContractDto.getAmountWithoutVat());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }
    
    GetRepairContractProjection createGetRepairContractAllOrganizationsProjection(RepairContractGetSelfOrganizationDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(repairGetContractDto.getAmountWithoutVat());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }
    
}