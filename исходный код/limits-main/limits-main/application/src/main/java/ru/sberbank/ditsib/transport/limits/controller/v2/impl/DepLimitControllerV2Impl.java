package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.controller.v2.DepLimitController;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitPrimaryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitReSharingByDepartmentV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Period;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.DepLimitMapper;
import ru.sberbank.ditsib.transport.limits.mapper.LimitMapper;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static ru.sberbank.ditsib.transport.constants.limits.LimitServiceType.getLimitServiceTypeByTransportType;

@RequiredArgsConstructor
@RestController
@Slf4j
@Scope("request")
class DepLimitControllerV2Impl extends BaseControllerImpl implements DepLimitController {

    private static final String CANNOT_SHARE_PREV_YEAR_ERROR = "Лимит за прошедшие годы не может быть перераспределен";

    private final EmployeeService employeeService;

    private final OrganizationService organizationService;

    private final DepartmentService departmentService;

    private final DepLimitService depLimitService;

    private final DepLimitMapper depLimitMapper;

    private final LimitSharingPercentService limitSharingPercentService;

    private final LimitMapper limitMapper;

    private final LimitService limitService;

    @Value("${spring.application.name}")
    private String appName;

    @CheckOrganizationAccess
    @Override
    public GetLimitV2DTO add(@Organization UUID organizationId, DepLimitPrimaryV2DTO depLimitPrimaryDTO, JwtAuthenticationToken authentication, String source) {
        var author = getEmployee(employeeService, authentication);
        if (depLimitPrimaryDTO.parentId() != null) {
            throw new LimitLogicException("Only upper-level limit can be added directly");
        }
        var organization =
                organizationService.get(organizationId)
                        .orElseThrow(() -> new EntityNotFoundException(ru.sberbank.ditsib.transport.limits.model.basic.Organization.class,
                                organizationId));
        var departmentList = departmentService.getUpperLevelDepartment(organizationId);
        if (departmentList.size() != 1) {
            throw new LimitLogicException("Cannot find unique upper level department. number of upper level departments is " + departmentList.size());
        }
        var department = departmentList.getFirst();
        log.debug("Found upper-level department with id {}", department.getId());
        var depLimit = depLimitService.getByDepartmentAndYearAndLimitServiceType(department.getId(),
                depLimitPrimaryDTO.year(),
                depLimitPrimaryDTO.limitServiceType());
        if (depLimit != null) {
            throw new LimitLogicException("Лимит на данную услугу уже существует в %d году".formatted(depLimitPrimaryDTO.year()));
        }

        Employee limitOwner = null;
        if (department.getDepartmentHead() != null) {
            limitOwner = employeeService.get(department.getDepartmentHead().getId()).orElse(null);
            department.setDepartmentHead(limitOwner);
        }
        depLimit = depLimitMapper.toModel(depLimitPrimaryDTO);
        depLimit.setAuthor(author);
        depLimit.setReserve(depLimitPrimaryDTO.sum());
        depLimit.setEconomy(BigDecimal.ZERO);
        depLimit.setParent(null);
        depLimit.setDepartment(department);
        depLimit.setOrganization(organization);
        depLimit.setLimitOwner(limitOwner);
        depLimit.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        depLimit = depLimitService.add(depLimit);

        if (LimitSharingType.PERCENTS.equals(depLimitPrimaryDTO.limitSharingType())) {
            var limitSharingPercents = getLimitSharingPercents(author, depLimit);
            limitSharingPercentService.add(limitSharingPercents);
        }
        return limitMapper.toDto(depLimit);
    }

    @Override
    public void reShareLimitDepartments(LimitReSharingByDepartmentV2DTO dto, JwtAuthenticationToken authentication, String sourceApp) {
        final var author = getEmployee(employeeService, authentication);
        final int currentYear = LocalDate.now(ZoneOffset.UTC).getYear();

        if (dto.year() < currentYear) {
            throw new LimitLogicException(CANNOT_SHARE_PREV_YEAR_ERROR);
        }
        // get source limit by department
        final var sourceDepLimit = getLimit(dto, dto.sourceDepartmentId());
        // get target limit by department
        final var targetDepLimit = getLimit(dto, dto.targetDepartmentId());

        if (sourceDepLimit.getParent() == null || targetDepLimit.getParent() == null) {
            throw new LimitLogicException("Головной лимит не может участвовать в перераспределении");
        }

        final var fromPeriod = Optional.ofNullable(dto.fromPeriod()).map(Period::toModel).orElse(null);
        final var toPeriod = Optional.ofNullable(dto.toPeriod()).map(Period::toModel).orElse(null);
        final var source = new LimitData(sourceDepLimit, dto.sourceTransportType(), fromPeriod);
        final var target = new LimitData(targetDepLimit, dto.targetTransportType(), toPeriod);
        final var sum = dto.sum();
        limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL,
                false);
    }

    private DepLimit getLimit(LimitReSharingByDepartmentV2DTO dto, UUID departmentId) {
        return Optional.ofNullable(depLimitService.getByDepartmentAndYearAndLimitServiceType(
                departmentId, dto.year(),
                getLimitServiceTypeByTransportType(dto.sourceTransportType()).name())).orElseThrow(() -> new EntityNotFoundException(Department.class, departmentId));
    }

    @NotNull
    private LimitSharingPercents getLimitSharingPercents(Employee author, DepLimit depLimit) {
        var limitSharingPercents = new LimitSharingPercents();
        limitSharingPercents.setJanuary(8);
        limitSharingPercents.setFebruary(8);
        limitSharingPercents.setMarch(8);
        limitSharingPercents.setApril(8);
        limitSharingPercents.setMay(8);
        limitSharingPercents.setJune(8);
        limitSharingPercents.setJuly(8);
        limitSharingPercents.setAugust(8);
        limitSharingPercents.setSeptember(8);
        limitSharingPercents.setOctober(8);
        limitSharingPercents.setNovember(8);
        limitSharingPercents.setDecember(12);
        limitSharingPercents.setAuthor(author);
        limitSharingPercents.setLimit(depLimit);
        return limitSharingPercents;
    }

}
