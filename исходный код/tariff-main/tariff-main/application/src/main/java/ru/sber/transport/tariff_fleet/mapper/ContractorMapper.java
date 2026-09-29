package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;

/**
 * Маппер контрагентов
 */
@Mapper(componentModel = "spring")
public interface ContractorMapper {

    @Mapping(target = "integrationType", source = "contractorType")
    ContractorDto contractorToContractorDto(Contractor source);
}
