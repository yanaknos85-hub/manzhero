package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(WebApplicationContext.SCOPE_REQUEST)
class LimitsExporter implements DataExporter<LimitDataFileDto> {

    private static final String ORGANIZATION_ID_PARAMETER = "organizationId";

    private final LimitService limitService;

    private final LimitSharingService limitSharingService;

    private final EmployeeService employeeService;

    @NotNull
    @Override
    public List<LimitDataFileDto> exportData(Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        log.debug("exportLimit: start");
        var organizationId = Optional.ofNullable(parameters.get(ORGANIZATION_ID_PARAMETER))
            .map(String::valueOf)
            .map(UUID::fromString)
            .orElseGet(() -> employeeService.getByUserId(UUID.fromString(authentication.getToken().getId()))
                .map(Employee::getOrganizationId).orElse(null));
        var year = Optional.ofNullable(parameters.get("year"))
            .map(String::valueOf)
            .map(Integer::parseInt)
            .orElseGet(() -> LocalDate.now(ZoneOffset.UTC).getYear());
        var startTime = LocalDateTime.now(ZoneOffset.UTC);
        var limitList = limitService.getByOrganizationIdAndYear(organizationId, year);
        var limitIdList = limitList.parallelStream().map(Limit::getId).toList();
        var limitSharingList = limitSharingService.getByLimitIds(limitIdList);
        log.debug("exportLimit: total limits {}, limitSharings {}", limitList.size(), limitSharingList.size());

        var result = limitService.getUpperLevelActiveLimit(organizationId, year)
            .parallelStream()
            .flatMap(l -> exportLimit(l, limitList, limitSharingList).parallelStream())
            .toList();
        log.debug("exportLimits: finish: time passed seconds: {}", Duration.between(startTime, LocalDateTime.now(ZoneOffset.UTC)).toSeconds());
        return result;
    }

    private List<LimitDataFileDto> exportLimit(Limit limit, List<Limit> limitList, List<LimitSharing> limitSharingList) {
        var result = new ArrayList<LimitDataFileDto>();
        result.addAll(getByLimit(limit, limitSharingList).parallelStream().map(this::writeLimitSharing).toList());
        result.addAll(getLimitChildren(limit, limitList).parallelStream().flatMap(c -> exportLimit(c, limitList, limitSharingList).parallelStream()).toList());
        return result;
    }

    private List<Limit> getLimitChildren(Limit limit, List<Limit> limitList) {
        return limitList.parallelStream().filter(e -> e.getParent() != null && e.getParent().getId().equals(limit.getId())).toList();
    }

    private List<LimitSharing> getByLimit(Limit limit, List<LimitSharing> limitSharingList) {
        return limitSharingList.parallelStream().filter(e -> e.getLimit().getId().equals(limit.getId())).toList();
    }

    private LimitDataFileDto writeLimitSharing(LimitSharing limitSharing) {
        var limit = limitService.get(limitSharing.getLimit().getId()).orElseThrow();
        return new LimitDataFileDto(
            "LIMITSHARING",
            limit instanceof DepLimit ? "DEP" : "EMP",
            limit instanceof DepLimit depLimit ? depLimit.getDepartment().getCode() : ((EmpLimit) limit).getEmployee().getPersonnelNumber(),
            limit instanceof DepLimit depLimit ? depLimit.getDepartment().getDepartmentName() : ((EmpLimit) limit).getEmployee().getFullName(),
            limit.getYear(),
            limitSharing.getTransportType().name(),
            limitSharing.getSum(),
            limitSharing.getBalance(),
            limit.getLimitStatus().name()
        );
    }

}
