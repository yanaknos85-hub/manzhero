package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitsStatsController;
import ru.sberbank.ditsib.transport.limits.dto.GeneralAnalyticalReportResponseDTO;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.analytic.ChartDTO;
import ru.sberbank.ditsib.transport.limits.dto.analytic.ChartType;
import ru.sberbank.ditsib.transport.limits.dto.analytic.MetricDTO;
import ru.sberbank.ditsib.transport.limits.dto.analytic.MetricType;
import ru.sberbank.ditsib.transport.limits.dto.v2.GeneralAnalyticalReportRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsService;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController("LimitsStatsControllerV2Impl")
@RequiredArgsConstructor
@Scope("request")
class LimitsStatsControllerImpl implements LimitsStatsController {

    private final DepLimitService depLimitService;

    private final LimitStatsService limitStatsService;

    private final EmployeeService employeeService;

    @CheckOrganizationAccess
    @Override
    public LimitStatsV2DTO getLimitStats(@Organization UUID organizationId, Integer year, Month month, Integer day, UUID departmentId) {
        var untilDate = getUntilDate(year, month, day);
        var limit = depLimitService.getByDepartmentAndYearAndLimitServiceType(departmentId, year, "PASSENGER");
        if (limit == null) {
            throw new EntityNotFoundException(Limit.class, Map.of("departmentId", departmentId, "year", untilDate.getYear()));
        }
        return limitStatsService.getLimitStatsV2(organizationId, List.of(new LimitLevelDTO(limit, 0)), untilDate)
                .stream().findFirst().orElse(null);
    }

    @CheckOrganizationAccess
    @Override
    public InputStreamResource getLimitStatsBytes(@Organization UUID organizationId, Integer year, Month month, Integer day, UUID departmentId) {
        try {
            var untilDate = getUntilDate(year, month, day);
            var limitList = limitStatsService.getLimitsList(organizationId, year, departmentId);
            var limitStatsDTOs = limitStatsService.getLimitStatsV2(
                    organizationId,
                    limitList.stream().filter(it -> it.getLimit() instanceof DepLimit).toList(),
                    untilDate);
            return new InputStreamResource(new FileInputStream(limitStatsService.exportToXlsxV2(limitStatsDTOs).toFile()));
        } catch (IOException e) {
            log.error("Download failed", e);
        }
        return new InputStreamResource(new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportDataV2(GeneralAnalyticalReportRequestV2DTO request, JwtAuthenticationToken authentication) {
        return getGeneralAnalyticalReportData(request, authentication);
    }

    @Override
    public GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(GeneralAnalyticalReportRequestV2DTO request, JwtAuthenticationToken authentication) {
        var organizationId = Optional.ofNullable(request.organizationId()).orElseGet(List::of);
        if (organizationId.isEmpty() || Boolean.FALSE.equals(authentication.getToken().getClaimAsBoolean("data_master"))) {
            organizationId = List.of(employeeService.getByUserId(UUID.fromString(authentication.getToken().getId())).orElseThrow().getOrganizationId());
        }
        int year = Optional.ofNullable(request.year()).orElseGet(() -> LocalDate.now(ZoneOffset.UTC).getYear());
        var transportTypes = request.transportType();
        if (transportTypes == null) {
            transportTypes = Arrays.asList(TransportTypeEnum.values());
        }
        var totalBudget = new AtomicReference<>(BigDecimal.ZERO);
        var spentBudget = new AtomicReference<>(BigDecimal.ZERO);
        var months = Optional.ofNullable(request.monthList())
                .map(m -> m.stream().map(Month::name).map(ru.sberbank.ditsib.transport.limits.model.limit.Month::valueOf).toList())
                .orElseGet(() -> Arrays.asList(ru.sberbank.ditsib.transport.limits.model.limit.Month.values()));

        final var numOfEmployees = BigDecimal.valueOf(employeeService.countByOrganizationIdAndActive(organizationId, true));

        var budgetsTypes = new ConcurrentHashMap<TransportTypeEnum, BigDecimal>();
        var spendsTypes = new ConcurrentHashMap<TransportTypeEnum, BigDecimal>();

        limitStatsService.getByOrganizationIdAndYearAndPeriodAndTransportType(
                        organizationId, year,
                        months,
                        transportTypes).parallelStream()
                .forEach(limitStat -> {
                    var budget = limitStat.budget();
                    var spending = limitStat.spending();

                    synchronized (this) {
                        totalBudget.set(totalBudget.get().add(budget));
                        spentBudget.set(spentBudget.get().add(spending));

                        budgetsTypes.computeIfPresent(limitStat.transportType(), (k, p) -> p.add(budget));
                        budgetsTypes.putIfAbsent(limitStat.transportType(), budget);

                        spendsTypes.computeIfPresent(limitStat.transportType(), (k, p) -> p.add(spending));
                        spendsTypes.putIfAbsent(limitStat.transportType(), spending);
                    }
                });
        var dataPerTransportTypeList = budgetsTypes.entrySet()
                .parallelStream().map(entry -> {
                    var transportType = entry.getKey();
                    var budget = entry.getValue();
                    var spent = spendsTypes.getOrDefault(transportType, BigDecimal.ZERO);
                    return createMetric(MetricType.BUDGET_PER_TRANSPORT_TYPE, budget, spent,
                            numOfEmployees.compareTo(BigDecimal.ZERO) > 0 ? budget.divide(numOfEmployees, 2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO,
                            numOfEmployees.compareTo(BigDecimal.ZERO) > 0 ? spent.divide(numOfEmployees, 2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO,
                            numOfEmployees.intValue(), transportType);
                })
                .toList();

        var totalMetric = createMetric(MetricType.BUDGET, totalBudget.get(), spentBudget.get(),
                numOfEmployees.compareTo(BigDecimal.ZERO) > 0 ? totalBudget.get().divide(numOfEmployees, 2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO,
                numOfEmployees.compareTo(BigDecimal.ZERO) > 0 ? spentBudget.get().divide(numOfEmployees, 2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO,
                numOfEmployees.intValue(), null);

        var chart = new ChartDTO(
                ChartType.BUDGET.getName(),
                List.of(totalMetric),
                dataPerTransportTypeList
        );
        return new GeneralAnalyticalReportResponseDTO(List.of(chart));
    }

    @NotNull
    private LocalDate getUntilDate(Integer year, Month month, Integer day) {
        var defaultDate = LocalDate.now(ZoneOffset.UTC).minusMonths(1);
        return LocalDate.of(
                Optional.ofNullable(year).orElse(defaultDate.getYear()),
                Optional.ofNullable(month).map(m -> java.time.Month.valueOf(m.name())).orElse(defaultDate.getMonth()),
                Optional.ofNullable(day).orElse(defaultDate.getDayOfMonth())
        );
    }

    private MetricDTO createMetric(MetricType metricType,
                                   BigDecimal totalBudget, BigDecimal spentBudget,
                                   BigDecimal totalBudgetPerEmployee, BigDecimal spentBudgetPerEmployee,
                                   Integer numOfEmployees, TransportTypeEnum transportType) {
        return new MetricDTO(
                metricType.getCode(),
                metricType.getName(),
                metricType.getTypeValue(),
                transportType,
                totalBudget,
                spentBudget,
                totalBudgetPerEmployee,
                spentBudgetPerEmployee,
                numOfEmployees
        );
    }

}
