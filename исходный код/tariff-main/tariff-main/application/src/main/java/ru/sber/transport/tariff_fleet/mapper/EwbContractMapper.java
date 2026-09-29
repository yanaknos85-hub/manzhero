package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.EwbContract;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbContractProjection;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractGetDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractPostDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbGetContractByIdDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;

import java.util.List;

/**
 * Маппер договоров ЭПЛ
 */
@Mapper(componentModel = "spring")
public interface EwbContractMapper {
    
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "organizationId", source = "contractDto.contractorOrganizationId")
    @Mapping(target = "amount", source = "contractDto.amount")
    @Mapping(target = "inspectionType", source = "contractDto.inspectionType")
    @Mapping(target = "edfOperatorId", source = "contractDto.edfOperatorId")
    @Mapping(target = "edfCode", source = "contractDto.edfCode")
    @Mapping(target = "organizationMedicalLicense.id", ignore = true)
    @Mapping(target = "organizationMedicalLicense", source = "contractDto.contractorMedicalLicense")
    EwbContract ewbContractPostDtoToEwbContract(EwbContractPostDto contractDto, Contract contract);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "start", source = "start")
    @Mapping(target = "end", source = "end")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "organizationName", source = "organizationName")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "inspectionType", source = "inspectionType")
    EwbContractGetDto getEwbContractProjectionToEwbContractGetDto(GetEwbContractProjection source);
    
    List<EwbContractGetDto> listGetEwbContractProjectionToListEwbContractGetDto(List<GetEwbContractProjection> source);
    
    @Mapping(target = "id", source = "contractId")
    @Mapping(target = "contractId", source = "contractId")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "inspectionType", source = "inspectionType")
    @Mapping(target = "edfOperatorId", source = "edfOperatorId")
    @Mapping(target = "edfCode", source = "edfCode")
    @Mapping(target = "organizationMedicalLicenseId", source = "organizationMedicalLicense.id")
    @Mapping(target = "active", source = "contract.active")
    @Mapping(target = "start", source = "contract.start")
    @Mapping(target = "end", source = "contract.end")
    EwbContractMessage ewbContractToEwbContractMessage(EwbContract source);
    
    @Mapping(target = "id", source = "source.contractId")
    @Mapping(target = "start", source = "source.contract.start")
    @Mapping(target = "end", source = "source.contract.end")
    @Mapping(target = "uvhd", source = "source.contract.uvhd")
    @Mapping(target = "number", source = "source.contract.number")
    @Mapping(target = "medicalLicenseSeries", source = "source.organizationMedicalLicense.series")
    @Mapping(target = "medicalLicenseNumber", source = "source.organizationMedicalLicense.number")
    @Mapping(target = "medicalLicenseIssueDate", source = "source.organizationMedicalLicense.issueDate")
    @Mapping(target = "medicalLicenseExpiryDate", source = "source.organizationMedicalLicense.expiryDate")
    EwbGetContractByIdDto ewbContractToEwbGetContractByIdDto(EwbContract source, String contractorName);
}
