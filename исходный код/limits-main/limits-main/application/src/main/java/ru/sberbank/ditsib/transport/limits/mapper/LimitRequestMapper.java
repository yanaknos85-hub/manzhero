package ru.sberbank.ditsib.transport.limits.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitRequestDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.EmpLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;

@Mapper(uses = { ApproverMapper.class, PeriodMapper.class })
public interface LimitRequestMapper {
    
    @Mapping(target = "period", source = "periodData", qualifiedByName = PeriodMapper.PERIOD_TO_INTEGER)
    @Mapping(target = "approverDtoList", source = "approverList")
    @Mapping(target = "limitId", ignore = true)
    @Mapping(target = "limitHumanreadableid", ignore = true)
    @Mapping(target = "limitSharingType", ignore = true)
    @Mapping(target = "plannedSum", ignore = true)
    @Mapping(target = "limitSum", ignore = true)
    @Mapping(target = "limitBalance", ignore = true)
    @Mapping(target = "parentLimitId", ignore = true)
    @Mapping(target = "parentLimitHumanreadableid", ignore = true)
    @Mapping(target = "parentLimitSum", ignore = true)
    @Mapping(target = "parentLimitBalance", ignore = true)
    GetLimitRequestDTO toDto(LimitRequest limitRequest);

    @Mapping(target = "period", source = "periodData", qualifiedByName = PeriodMapper.PERIOD_TO_DTO)
    @Mapping(target = "approverDtoList", source = "approverList")
    @Mapping(target = "limitId", ignore = true)
    @Mapping(target = "limitHumanreadableid", ignore = true)
    @Mapping(target = "limitSharingType", ignore = true)
    @Mapping(target = "plannedSum", ignore = true)
    @Mapping(target = "limitSum", ignore = true)
    @Mapping(target = "limitBalance", ignore = true)
    @Mapping(target = "parentLimitId", ignore = true)
    @Mapping(target = "parentLimitHumanreadableid", ignore = true)
    @Mapping(target = "parentLimitSum", ignore = true)
    @Mapping(target = "parentLimitBalance", ignore = true)
    GetLimitRequestV2DTO toV2Dto(LimitRequest limitRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "statusCode", ignore = true)
    @Mapping(target = "declineReason", ignore = true)
    @Mapping(target = "approverList", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "periodData", source = "period")
    @Mapping(target = "limitType", constant = "DEPARTMENT")
    @Mapping(target = "status", constant = "INIT")
    LimitRequest toModel(DepLimitRequestV2DTO source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "statusCode", ignore = true)
    @Mapping(target = "declineReason", ignore = true)
    @Mapping(target = "approverList", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "askTargets", ignore = true)
    @Mapping(target = "periodData", source = "period")
    @Mapping(target = "limitType", constant = "EMPLOYEE")
    @Mapping(target = "status", constant = "INIT")
    LimitRequest toModel(EmpLimitRequestV2DTO source);

    @Mapping(target = "period", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "askTargets", ignore = true)
    void update(@MappingTarget LimitRequest target, DepLimitRequestV2DTO source);

    @Mapping(target = "period", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "askTargets", ignore = true)
    void update(@MappingTarget LimitRequest target, EmpLimitRequestV2DTO source);
}
