package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.projection.GetFuelContractProjection;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.fuel.*;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelContractMessage;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;

import java.util.UUID;

/**
 * Маппер договоров по ремонту
 */
@Mapper(componentModel = "spring", imports = Organization.class, uses = {ServicePointMapper.class})
public interface FuelContractMapper {

    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "contractorId", source = "contractDto.contractorId")
    @Mapping(target = "servicePointsName", source = "contractDto.name")
    @Mapping(target = "organization", expression = "java(Organization.builder().id(contractDto.getOrganizationId()).build())")
    FuelContract fuelContractPostDtoToFuelContract(FuelContractPostAllOrganizationsDto contractDto, Contract contract);

    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "contractorId", source = "contractDto.contractorId")
    @Mapping(target = "servicePointsName", source = "contractDto.name")
    @Mapping(target = "organization", expression = "java(Organization.builder().id(organizationId).build())")
    FuelContract fuelContractPostSelfOrganizationDtoToFuelContract(
            FuelContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId
    );

    @Mapping(target = "documentType", constant = "FUEL")
    @Mapping(target = "amountWithoutVat", source = "amount")
    FuelContractGetAllOrganizationsDto getFuelContractProjectionToFuelContractGetAllOrganizationsDto(GetFuelContractProjection source);

    @Mapping(target = "documentType", constant = "FUEL")
    @Mapping(target = "amountWithoutVat", source = "amount")
    FuelContractGetSelfOrganizationDto getFuelContractProjectionToFuelContractGetSelfOrganizationDto(GetFuelContractProjection source);

    @Mapping(target = "amountWithVatOpt", expression = "java(Optional.ofNullable(source.getAmountWithVat()))")
    @Mapping(target = "amountWithoutVatOpt", expression = "java(Optional.ofNullable(source.getAmountWithoutVat()))")
    @Mapping(target = "logoOpt", expression = "java(Optional.ofNullable(source.getLogo()))")
    @Mapping(target = "servicePointsOpt", expression = "java(Optional.ofNullable(source.getServicePoints()))")
    @Mapping(target = "servicePointsNameOpt", expression = "java(Optional.ofNullable(source.getName()))")
    PartiallyUpdateFuelContractModel toPartiallyUpdateFuelContractModel(FuelContractUpdateDto source);

    @Mapping(target = "id", source = "fuelContract.contractId")
    @Mapping(target = "contractorName", source = "fuelContract.contractor.name")
    @Mapping(target = "organizationName", source = "fuelContract.organization.officialName")
    @Mapping(target = "number", source = "fuelContract.contract.number")
    @Mapping(target = "start", source = "fuelContract.contract.start")
    @Mapping(target = "end", source = "fuelContract.contract.end")
    @Mapping(target = "active", source = "fuelContract.contract.active")
    @Mapping(target = "logo", source = "logoFileBase64")
    @Mapping(target = "file", source = "servicePointsFileBase64")
    @Mapping(target = "name", source = "fuelContract.servicePointsName")
    @Mapping(target = "isEditablePoints", source = "isEditable")
    ContractWithServicePointsFileDto toContractWithServicePointsFileDto(FuelContract fuelContract, String servicePointsFileBase64, String logoFileBase64, boolean isEditable);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "number", source = "contract.number")
    @Mapping(target = "start", source = "contract.start")
    @Mapping(target = "end", source = "contract.end")
    @Mapping(target = "active", source = "contract.active")
    FuelContractMessage toFuelContractMessage(FuelContract source);
}
