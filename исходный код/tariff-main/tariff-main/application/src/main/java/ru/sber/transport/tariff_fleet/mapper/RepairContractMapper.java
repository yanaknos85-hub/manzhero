package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.database.projection.GetRepairContractProjection;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairContractMessage;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;

import java.util.List;
import java.util.UUID;

/**
 * Маппер договоров по ремонту
 */
@Mapper(componentModel = "spring", imports = Organization.class, uses = {ServicePointMapper.class})
public interface RepairContractMapper {

    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "contractorId", source = "contractDto.contractorId")
    @Mapping(target = "servicePointsName", source = "contractDto.name")
    @Mapping(target = "organization", expression = "java(Organization.builder().id(contractDto.getOrganizationId()).build())")
    RepairContract repairContractPostDtoToRepairContract(RepairContractPostAllOrganizationsDto contractDto, Contract contract);

    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "contract", source = "contract")
    @Mapping(target = "contractorId", source = "contractDto.contractorId")
    @Mapping(target = "servicePointsName", source = "contractDto.name")
    @Mapping(target = "organization", expression = "java(Organization.builder().id(organizationId).build())")
    RepairContract repairContractPostSelfOrganizationDtoToRepairContract(
            RepairContractPostSelfOrganizationDto contractDto, Contract contract, UUID organizationId
    );

    RepairContractGetDto getRepairContractProjectionToRepairGetContractDto(GetRepairContractProjection source);

    @Mapping(target = "documentType", constant = "REPAIR_AND_MAINTENANCE")
    @Mapping(target = "amountWithoutVat", source = "amount")
    RepairContractGetAllOrganizationsDto getRepairContractProjectionToRepairContractGetAllOrganizationsDto(GetRepairContractProjection source);

    @Mapping(target = "documentType", constant = "REPAIR_AND_MAINTENANCE")
    @Mapping(target = "amountWithoutVat", source = "amount")
    RepairContractGetSelfOrganizationDto getRepairContractProjectionToRepairContractGetSelfOrganizationDto(GetRepairContractProjection source);

    @Mapping(target = "amountWithVatOpt", expression = "java(Optional.ofNullable(source.getAmountWithVat()))")
    @Mapping(target = "amountWithoutVatOpt", expression = "java(Optional.ofNullable(source.getAmountWithoutVat()))")
    @Mapping(target = "logoOpt", expression = "java(Optional.ofNullable(source.getLogo()))")
    @Mapping(target = "servicePointsOpt", expression = "java(Optional.ofNullable(source.getServicePoints()))")
    @Mapping(target = "servicePointsNameOpt", expression = "java(Optional.ofNullable(source.getName()))")
    PartiallyUpdateRepairContractModel toPartiallyUpdateRepairContractModel(RepairContractUpdateDto source);

    @Mapping(target = "id", source = "repairContract.contractId")
    @Mapping(target = "contractorName", source = "repairContract.contractor.name")
    @Mapping(target = "organizationName", source = "repairContract.organization.officialName")
    @Mapping(target = "number", source = "repairContract.contract.number")
    @Mapping(target = "start", source = "repairContract.contract.start")
    @Mapping(target = "end", source = "repairContract.contract.end")
    @Mapping(target = "active", source = "repairContract.contract.active")
    @Mapping(target = "logo", source = "logoFileBase64")
    @Mapping(target = "file", source = "servicePointsFileBase64")
    @Mapping(target = "name", source = "repairContract.servicePointsName")
    @Mapping(target = "isEditablePoints", source = "isEditable")
    ContractWithServicePointsFileDto toContractWithServicePointsFileDto(
            RepairContract repairContract, String servicePointsFileBase64, String logoFileBase64, boolean isEditable
    );

    @Mapping(target = "start", source = "contract.start")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "number", source = "contract.number")
    @Mapping(target = "end", source = "contract.end")
    @Mapping(target = "uvhd", source = "contract.uvhd")
    @Mapping(target = "id", source = "contractId")
    @Mapping(target = "active", source = "contract.active")
    @Mapping(target = "servicePoints", source = "source", qualifiedByName = "mapServicePoints")
    RepairContractMessage toRepairContractMessage(RepairContract source);

    @Named("mapServicePoints")
    default List<RepairContractMessage.ServicePoint> mapServicePoints(RepairContract source) {
        if (CollectionUtils.isEmpty(source.getServicePoints())) {
            return List.of();
        }
        return source.getServicePoints().stream()
                .map(sp -> new RepairContractMessage.ServicePoint(
                        sp.getId(),
                        sp.getAddress(),
                        sp.getLatitude(),
                        sp.getLongitude(),
                        sp.isActive(),
                        source.getLogoS3Id()
                ))
                .toList();
    }

}
