package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.EwbContract;
import ru.sber.transport.tariff_fleet.database.model.OrganizationMedicalLicense;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbContractProjection;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractGetDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPostDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbGetContractByIdDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class EwbContractMapperTest {
    
    private final EwbContractMapper ewbContractMapper = Mappers.getMapper(EwbContractMapper.class);
    
    @Test
    void ewbPostContractDtoToEwbContract() {
        var contractDto = Instancio.create(EwbContractPostDto.class);
        var contract = Instancio.create(Contract.class);
        var expected = new EwbContract(
                                       contract,
                                       contractDto.getContractorOrganizationId(),
                                       contractDto.getAmount(),
                                       contractDto.getInspectionType(),
                                       contractDto.getEdfOperatorId(),
                                       contractDto.getEdfCode(),
                                       new OrganizationMedicalLicense(null,
                                                                      contractDto.getContractorMedicalLicense().series(),
                                                                      contractDto.getContractorMedicalLicense().number(),
                                                                      contractDto.getContractorMedicalLicense().issueDate(),
                                                                      contractDto.getContractorMedicalLicense().expiryDate()));
        var actual = ewbContractMapper.ewbContractPostDtoToEwbContract(contractDto, contract);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(ewbContractMapper.ewbContractPostDtoToEwbContract(null, null)).isNull();
    }
    
    @Test
    void getEwbContractProjectionToEwbGetContractDto() {
        var expected = Instancio.create(EwbContractGetDto.class);
        var projection = createGetEwbContractProjection(expected);
        var actual = ewbContractMapper.getEwbContractProjectionToEwbContractGetDto(projection);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(ewbContractMapper.getEwbContractProjectionToEwbContractGetDto(null)).isNull();
    }
    
    @Test
    void listGetEwbContractProjectionToListEwbGetContractDto() {
        var expected1 = Instancio.create(EwbContractGetDto.class);
        var expected2 = Instancio.create(EwbContractGetDto.class);
        var projection1 = createGetEwbContractProjection(expected1);
        var projection2 = createGetEwbContractProjection(expected2);
        var actual = ewbContractMapper.listGetEwbContractProjectionToListEwbContractGetDto(List.of(projection1, projection2));
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected1, expected2));
        assertThat(ewbContractMapper.listGetEwbContractProjectionToListEwbContractGetDto(null)).isNull();
    }
    
    @Test
    void ewbContractToEwbContractMessage() {
        var source = Instancio.of(EwbContract.class)
                              .set(field(EwbContract::getContract), Instancio.of(Contract.class)
                                                                             .set(field(Contract::isActive), true)
                                                                             .create())
                              .create();
        var expected = new EwbContractMessage(source.getContractId(),
                                              source.getContractId(),
                                              source.getOrganizationId(),
                                              source.getInspectionType(),
                                              source.getEdfOperatorId(),
                                              source.getEdfCode(),
                                              source.getOrganizationMedicalLicense().getId(),
                                              source.getContract().getStart(),
                                              source.getContract().getEnd(),
                                              true);
        var actual = ewbContractMapper.ewbContractToEwbContractMessage(source);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(ewbContractMapper.ewbContractToEwbContractMessage(null)).isNull();
    }
    
    @Test
    void ewbContractToEwbGetContractByIdDto() {
        var ewbContract = Instancio.create(EwbContract.class);
        var contractorName = Instancio.create(String.class);
        var expected = new EwbGetContractByIdDto(
                ewbContract.getContractId(),
                ewbContract.getContract().getNumber(),
                ewbContract.getContract().getUvhd(),
                ewbContract.getContract().getStart(),
                ewbContract.getContract().getEnd(),
                contractorName,
                ewbContract.getAmount(),
                ewbContract.getInspectionType(),
                ewbContract.getEdfOperatorId(),
                ewbContract.getEdfCode(),
                ewbContract.getOrganizationMedicalLicense().getSeries(),
                ewbContract.getOrganizationMedicalLicense().getNumber(),
                ewbContract.getOrganizationMedicalLicense().getIssueDate(),
                ewbContract.getOrganizationMedicalLicense().getExpiryDate());
        var actual = ewbContractMapper.ewbContractToEwbGetContractByIdDto(ewbContract, contractorName);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    
    private GetEwbContractProjection createGetEwbContractProjection(EwbContractGetDto ewbGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetEwbContractProjection.class);
        projection.setId(ewbGetContractDto.getId());
        projection.setNumber(ewbGetContractDto.getNumber());
        projection.setStart(ewbGetContractDto.getStart());
        projection.setEnd(ewbGetContractDto.getEnd());
        projection.setActive(ewbGetContractDto.isActive());
        projection.setOrganizationName(ewbGetContractDto.getContractorOrganizationName());
        projection.setInspectionType(ewbGetContractDto.getInspectionType());
        projection.setAmount(ewbGetContractDto.getAmount());
        return projection;
    }
}