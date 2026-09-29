package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostDto;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostSelfOrganizationDto;

/**
 * Маппер договоров
 */
@Mapper(componentModel = "spring")
public interface ContractMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "creatorUserId", ignore = true)
    @Mapping(target = "start", source = "abstractContractPostDto.period.start")
    @Mapping(target = "end", source = "abstractContractPostDto.period.end")
    @Mapping(target = "number", source = "abstractContractPostDto.number")
    @Mapping(target = "uvhd", source = "abstractContractPostDto.uvhd")
    @Mapping(target = "active", source = "active")
    Contract abstractPostContractDtoToContract(AbstractContractPostDto abstractContractPostDto, boolean active);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "creatorUserId", ignore = true)
    @Mapping(target = "number", source = "abstractContractPostAllOrganizationsDto.number")
    @Mapping(target = "uvhd", source = "abstractContractPostAllOrganizationsDto.uvhd")
    @Mapping(target = "active", source = "active")
    Contract abstractContractPostAllOrganizationsDtoToContract(AbstractContractPostAllOrganizationsDto abstractContractPostAllOrganizationsDto, boolean active);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "creatorUserId", ignore = true)
    @Mapping(target = "number", source = "abstractContractPostSelfOrganizationDto.number")
    @Mapping(target = "uvhd", source = "abstractContractPostSelfOrganizationDto.uvhd")
    @Mapping(target = "active", source = "active")
    Contract abstractContractPostSelfOrganizationDtoToContract(AbstractContractPostSelfOrganizationDto abstractContractPostSelfOrganizationDto, boolean active);
}
