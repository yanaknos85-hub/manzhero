package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.RepairTariff;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairTariffMessage;

/**
 * Маппер тарифов по ремонту
 */
@Mapper(componentModel = "spring", imports = Organization.class, uses = {ServicePointMapper.class})
public interface RepairTariffMapper {

    @Mapping(target = "isFieldService", source = "source.fieldService")
    @Mapping(target = "id", source = "source.tariffId")
    @Mapping(target = "contractId", source = "source.tariff.contractId")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "departmentId", source = "source.department.id")
    RepairTariffMessage toRepairTariffMessage(RepairTariff source, boolean active);
}
