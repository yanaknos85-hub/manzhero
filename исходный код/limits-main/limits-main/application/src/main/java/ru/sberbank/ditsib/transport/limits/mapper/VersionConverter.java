package ru.sberbank.ditsib.transport.limits.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.*;
import ru.sberbank.ditsib.transport.limits.model.*;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.service.LimitService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(uses = {PeriodMapper.class, DateMapper.class})
public interface VersionConverter {

    @Mapping(target = "period", expression = "java(source.getPeriod().ordinal())")
    @Mapping(target = "procentUsedPeriod", source = "percentUsedPeriod")
    @Mapping(target = "procentUsedYear", source = "percentUsedYear")
    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    LimitStatsDTO toV1(LimitStatsV2DTO source);

    @Mapping(target = "procentUsedPeriod", source = "percentUsedPeriod")
    @Mapping(target = "procentSpentPeriod", source = "percentSpentPeriod")
    @Mapping(target = "procentUsed", source = "percentUsed")
    @Mapping(target = "procentSpent", source = "percentSpent")
    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    LimitSharingStatsDTO toV1(LimitSharingStatsV2DTO source);

    @Mapping(target = "period", expression = "java(source.period().ordinal())")
    EconomyDTO toV1(EconomyV2DTO source);

    @Mapping(target = "period", expression = "java(source.period().ordinal())")
    @Mapping(target = "counterpartPeriod", expression = "java(source.counterpartPeriod().ordinal())")
    GetLimitHistoryDTO toV1(GetLimitHistoryV2DTO source);

    GetLimitTransferHistoryDTO toV1(GetLimitTransferHistoryV2DTO source);

    GetLimitSharingPercentsDTO toV1(GetLimitSharingPercentsV2DTO source);

    @Mapping(target = "periodNumber", expression = "java(source.period().ordinal())")
    GetLimitSharingPerPeriodDTO toV1(GetLimitSharingPerPeriodV2DTO source);

    @Mapping(target = "limitSharingPerPeriodDTO", expression = "java(toV1Current(source.sharings()))")
    GetLimitSharingDTO toV1(GetLimitSharingV2DTO source);

    @Mapping(target = "period", expression = "java(source.period().ordinal())")
    GetLimitRequestDTO toV1(GetLimitRequestV2DTO source);

    @Mapping(target = "limitSharingDTOList", source = "sharings")
    GetLimitDTO toV1(GetLimitV2DTO getLimitV2DTO);

    @Mapping(target = "limitSharingDTOList", ignore = true)
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "employee", source = "employeeId")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "limitOwner", source = "ownerId")
    @Mapping(target = "limitServiceType", source = "serviceType")
    @Mapping(target = "limitSharingType", source = "sharingType")
    @Mapping(target = "limitStatus", source = "status")
    @Mapping(target = "limitType", source = "type")
    @Mapping(target = "parentLimitId", source = "parentId")
    GetLimitDTO toV1(ru.sber.transport.limits.business.model.Limit source);

    default GetEmployeeDTO toEmployee(UUID id) {
        if (id == null) {
            return null;
        }
        final var employee = new GetEmployeeDTO();
        employee.setId(id);
        return employee;
    }

    @Mapping(target = "departmentName", source = "name")
    GetDepartmentDTO toV1(GetDepartmentV2DTO getLimitV2DTO);

    default GetLimitSharingPerPeriodDTO toV1Current(List<GetLimitSharingPerPeriodV2DTO> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        var period = source.getFirst().period();
        var currentPeriod = Period.create(LocalDate.now(ZoneOffset.UTC), period.getClass());
        return source.stream()
                .filter(dto -> dto.period().equals(currentPeriod))
                .findFirst()
                .map(this::toV1)
                .orElse(null);
    }

    @Mapping(target = "sourcePeriod", source = "source", qualifiedByName = "fromSourceLimit")
    @Mapping(target = "targetPeriod", source = "source", qualifiedByName = "fromTargetLimit")
    LimitTransferHistoryV2DTO toV2(LimitTransferHistoryDTO source);

    LimitSharingPercentsV2DTO toV2(LimitSharingPercentsDTO source);

    @Mapping(target = "monthList", source = "monthList", qualifiedByName = PeriodMapper.INT_TO_MONTH)
    @Mapping(target = "organizationId", source = "organizationId")
    GeneralAnalyticalReportRequestV2DTO toV2(GeneralAnalyticalReportRequestDTO request);

    @Mapping(target = "period", source = "period", qualifiedByName = PeriodMapper.INT_TO_MONTH)
    EmpLimitRequestV2DTO toV2(EmpLimitRequestDTO source);

    @Mapping(target = "period", source = "period", qualifiedByName = PeriodMapper.INT_TO_MONTH)
    DepLimitRequestV2DTO toV2(DepLimitRequestDTO source);

    DepLimitPrimaryV2DTO toV2(DepLimitPrimaryDTO source);

    @Mapping(target = "fromPeriod", source = "fromPeriod", qualifiedByName = PeriodMapper.INT_TO_MONTH)
    @Mapping(target = "toPeriod", source = "toPeriod", qualifiedByName = PeriodMapper.INT_TO_MONTH)
    LimitReSharingByDepartmentV2DTO toV2(LimitReSharingByDepartmentDTO source);

    @Named("fromSourceLimit")
    default ru.sberbank.ditsib.transport.limits.dto.v2.Period fromSourceLimit(LimitTransferHistoryDTO source) {
        return fromLimit(source.getSourceLimit(), source.getSourcePeriod());
    }

    @Named("fromTargetLimit")
    default ru.sberbank.ditsib.transport.limits.dto.v2.Period fromTargetLimit(LimitTransferHistoryDTO source) {
        return fromLimit(source.getTargetLimit(), source.getTargetPeriod());
    }

    default ru.sberbank.ditsib.transport.limits.dto.v2.Period fromLimit(UUID limitId, Integer period) {
        if (period == null) {
            return null;
        }
        var type = limitService().get(limitId).map(Limit::getLimitSharingType).orElseThrow();
        if (type == LimitSharingType.QUARTER) {
            return Quarter.values()[period];
        }
        return Month.values()[period];
    }

    default ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Lookup
    default LimitService limitService() {
        return null;
    }
}
