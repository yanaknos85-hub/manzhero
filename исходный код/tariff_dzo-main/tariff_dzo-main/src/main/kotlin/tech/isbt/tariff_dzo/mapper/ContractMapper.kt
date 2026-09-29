package tech.isbt.tariff_dzo.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import tech.isbt.tariff_dzo.dto.common.ContractType
import tech.isbt.tariff_dzo.dto.contract.ContractIncomeCreateRequest
import tech.isbt.tariff_dzo.dto.contract.ContractDto
import tech.isbt.tariff_dzo.dto.contract.ContractOutcomeCreateRequest

@Mapper(componentModel = "spring")
interface ContractMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "organizationNames", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "region", expression = "java(\"\")")
    @Mapping(target = "userId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "creationTime", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "tariffsExist", constant = "false")
    @Mapping(target = "regionIds", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "contractType", source = "contractType")
    fun toDto(request: ContractIncomeCreateRequest, contractType: ContractType): ContractDto

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "organizationNames", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "region", expression = "java(\"\")")
    @Mapping(target = "userId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "creationTime", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "tariffsExist", constant = "false")
    @Mapping(target = "regionIds", expression = "java(java.util.Collections.emptyList())")
    @Mapping(target = "contractType", source = "contractType")
    fun toDto(request: ContractOutcomeCreateRequest, contractType: ContractType): ContractDto
}
