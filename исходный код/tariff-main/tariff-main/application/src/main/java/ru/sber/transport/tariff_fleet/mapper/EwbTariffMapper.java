package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffByIdProjection;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffProjection;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetByIdDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;

import java.util.List;
import java.util.UUID;

/**
 * Маппер тарифов ЭПЛ
 */
@Mapper(componentModel = "spring")
public interface EwbTariffMapper {
    
    @Mapping(target = "tariffId", ignore = true)
    @Mapping(target = "tariff", source = "tariff")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "departmentId", source = "tariffDto.departmentId")
    @Mapping(target = "amount", source = "tariffDto.amount")
    EwbTariff ewbPostTariffDtoToEwbTariff(EwbTariffPostDto tariffDto, Tariff tariff, UUID organizationId);
    
    
    @Mapping(target = "id", source = "tariffId")
    @Mapping(target = "contractId", source = "tariff.contractId")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "departmentId", source = "departmentId")
    @Mapping(target = "active", source = "tariff.active")
    EwbTariffMessage ewbTariffToEwbTariffMessage(EwbTariff source);
    
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "humanReadableId", source = "humanReadableId")
    @Mapping(target = "inspectionType", source = "inspectionType")
    @Mapping(target = "organizationName", source = "organizationName")
    @Mapping(target = "departmentName", source = "departmentName")
    @Mapping(target = "contractOrganizationName", source = "contractOrganizationName")
    @Mapping(target = "contractNumber", source = "contractNumber")
    EwbTariffGetDto getEwbTariffProjectionToEwbGetTariffDto(GetEwbTariffProjection source);
    
    List<EwbTariffGetDto> listGetEwbTariffProjectionToListEwbGetTariffDto(List<GetEwbTariffProjection> source);
    
    EwbTariffGetByIdDto getEwbTariffByIdProjectionToEwbTariffGetByIdDto(GetEwbTariffByIdProjection source);
}
