package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.database.projection.GetTariffProjection;
import ru.sber.transport.tariff_fleet.dto.AbstractCreateTariffRequest;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPostDto;
import ru.sber.transport.tariff_fleet.dto.SearchTariffRequest;
import ru.sber.transport.tariff_fleet.dto.SearchTariffResponse;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelTariffDto;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairTariffDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelTariffMessage;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TariffMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", source = "active")
    @Mapping(target = "activationType", constant = "AUTO")
    @Mapping(target = "humanReadableId", source = "humanReadableId")
    Tariff abstractPostTariffDtoToTariff(AbstractTariffPostDto postTariffDto, String humanReadableId, boolean active);

    @Mapping(target = "creatorUserId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "activationType", constant = "MANUAL")
    @Mapping(target = "humanReadableId", source = "humanReadableId")
    Tariff createTariffRequestToTariff(AbstractCreateTariffRequest request, String humanReadableId);

    @Mapping(target = "fieldService", source = "tariff.fieldService")
    @Mapping(target = "id", source = "tariff.tariff.id")
    @Mapping(target = "humanReadableId", source = "tariff.tariff.humanReadableId")
    @Mapping(target = "documentType", constant = "REPAIR_AND_MAINTENANCE")
    @Mapping(target = "organizationName", source = "officialName")
    @Mapping(target = "contractorName", source = "contract.contractor.name")
    @Mapping(target = "departmentName", source = "tariff.department.departmentName")
    @Mapping(target = "contractNumber", source = "contract.contract.number")
    RepairTariffDto repairTariffToRepairTariffDto(RepairTariff tariff, RepairContract contract, String officialName);

    @Mapping(target = "id", source = "tariff.tariff.id")
    @Mapping(target = "humanReadableId", source = "tariff.tariff.humanReadableId")
    @Mapping(target = "documentType", constant = "FUEL")
    @Mapping(target = "organizationName", source = "officialName")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "contractorName", source = "contract.contractor.name")
    @Mapping(target = "contractNumber", source = "contract.contract.number")
    FuelTariffDto fuelTariffToFuelTariffDto(FuelTariff tariff, FuelContract contract, String officialName, Department department);

    @Mapping(target = "tariffId", ignore = true)
    @Mapping(target = "tariff", source = "tariff")
    @Mapping(target = "isFieldService", expression = "java(request.isFieldService())")
    @Mapping(target = "department.id", source = "request.departmentId")
    RepairTariff createRepairTariffRequestToRepairTariff(CreateRepairTariffDto request, Tariff tariff, Contract contract, UUID organizationId);

    @Mapping(target = "tariffId", ignore = true)
    FuelTariff createFuelTariffRequestToFuelTariff(CreateFuelTariffDto request, Tariff tariff, Department department, Contract contract, UUID organizationId);

    @Mapping(target = "contractorId", source = "source.contractorId")
    @Mapping(target = "contractId", source = "source.contractId")
    @Mapping(target = "organizationId", source = "source.organizationId")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "active", source = "source.active")
    TariffFilter searchTariffRequestToTariffFilter(SearchTariffRequest source);

    @Mapping(target = "contractorId", source = "source.contractorId")
    @Mapping(target = "contractId", source = "source.contractId")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    TariffFilter searchTariffRequestToTariffFilter(SearchTariffRequest source, UUID organizationId);

    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "humanReadableId", source = "source.humanReadableId")
    @Mapping(target = "organizationName", source = "source.organizationName")
    @Mapping(target = "contractorName", source = "source.contractorName")
    @Mapping(target = "departmentName", source = "source.departmentName")
    @Mapping(target = "contractNumber", source = "source.contractNumber")
    @Mapping(target = "active", source = "source.active")
    SearchTariffResponse getTariffProjectionToSearchTariffResponse(GetTariffProjection source, DocumentType documentType);

    @Mapping(target = "contractId", source = "fuelTariff.tariff.contractId")
    @Mapping(target = "active", source = "fuelTariff.tariff.active")
    @Mapping(target = "humanReadableId", source = "fuelTariff.tariff.humanReadableId")
    FuelTariffMessage toMessage(FuelTariff fuelTariff);
}
