package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dto.*;
import ru.sberbank.ditsib.transport.limits.model.GetLimitDTO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Controller interface for employee limits.
 */
@RequestMapping(value = {"/limits", "/limits/"})
@Tag(name = "Лимиты", description = "Набор операций для работы с лимитами")
public interface LimitController {

    /**
     * Get limits by filter.
     *
     * @return list of limits.
     * @deprecated use `GET /?organizationId={organizationId}&amp;...` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/search/{organizationId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск лимитов", description = "Поиск лимитов")
    Collection<GetLimitDTO> search(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody LimitSearchDTO limitSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get limits by filter.
     *
     * @return list of limits.
     * @deprecated use `GET /?organizationId={organizationId}&amp;...` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = "/searchPageable/{organizationId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск лимитов", description = "Поиск лимитов")
    Page<GetLimitDTO> search(
            @PathVariable("organizationId") UUID organizationId,
            @Valid @RequestBody LimitSearchDTO limitSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @PageableDefault(size = 20, sort = { "creationTime" }, direction = Sort.Direction.ASC)
                    Pageable pageable);

    /**
     * Limits export
     *
     * @param year year
     *
     * @return result of export
     * @deprecated use `GET /statistic?organizationId={organizationId}&amp;year={year}&amp;month={month}&amp;day={day}&amp;departmentId={departmentId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = {"/getstats/{organizationId}/{year}/{month}/{day}/{departmentId}", "/getstats/{organizationId}/{year}/{month}/{day}/{departmentId}/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Экспорт статистики по лимитам", description = "Экспорт статистики по лимитам")
    LimitStatsDTO getLimitStats(@PathVariable("organizationId") UUID organizationId,
                                @PathVariable("year") Integer year,
                                @PathVariable("month") Integer month,
                                @PathVariable("day") Integer day,
                                @PathVariable("departmentId") UUID departmentId,
                                @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Limits export
     *
     * @param year year
     *
     * @return result of export
     * @deprecated use `GET /statistic?organizationId={organizationId}&amp;year={year}&amp;departmentId={departmentId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = {"/getstats/{organizationId}/{year}/{departmentId}", "/getstats/{organizationId}/{year}/{departmentId}/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Экспорт статистики по лимитам", description = "Экспорт статистики по лимитам")
    LimitStatsDTO getLimitStats(@PathVariable("organizationId") UUID organizationId,
                                @PathVariable("year") Integer year,
                                @PathVariable("departmentId") UUID departmentId,
                                @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Получение данных по общему аналитическому отчету
     * @param request параметры запроса в виде JSON
     *        year - год, за который нужно получить данные
     * @deprecated use `GET /statistic` instead
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = {"/getstats/general", "/getstats/general/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить данные аналитического отчета", description = "Получить данные аналитического отчета")
    GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            @RequestBody @Valid GeneralAnalyticalReportRequestDTO request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get limit with ID.
     *
     * @param limitId ID of limit to get.
     *
     * @return request.
     */
    @GetMapping(value = {"/spendings/{organizationId}/{limitId}/{maxRecords}", "/spendings/{organizationId}/{limitId}/{maxRecords}/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Расходы по лимиту", description = "Получение расходов по лимиту")
    List<LimitSpendingDTO> getSpendings(@PathVariable("organizationId") UUID organizationId,
                                                  @PathVariable("limitId") @NotNull UUID limitId,
                                                  @PathVariable("maxRecords") @NotNull Integer maxRecords);



    /**
     * Get transfers by limit.
     *
     * @param limitId ID of limit.
     *
     * @return transfers.
     * @deprecated use `GET /history?organizationId={organizationId}&amp;limitId={limitId}&amp;year={year}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = {"/getHistory/{organizationId}/{limitId}/{year}", "/getHistory/{organizationId}/{limitId}/{year}/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Движение ДС по лимиту", description = "Получение движений ДС по лимиту")
    Page<GetLimitHistoryOldDTO> getLimitHistoryOld(@PathVariable("organizationId") UUID organizationId,
                                                       @PathVariable("limitId") @NotNull UUID limitId,
                                                       @PathVariable("year") @NotNull Integer year,
                                                       @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable);

    /**
     * Gets limit connected with request.
     *
     * @param requestId ID of request.
     *
     * @return limit.
     * @deprecated use `GET /?requestId={requestId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = {"/limitbyrequest/{requestId}", "/limitbyrequest/{requestId}/"},
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Лимит связанный с заявкой на поездку",
               description = "Лимит связанный с заявкой на поездку")
    GetLimitDTO getLimitByRequest(@PathVariable("requestId") @NotNull UUID requestId,
                                  JwtAuthenticationToken token);

    /**
     * Get remains per period
     *
     * @param requestList requestList.
     *
     * @return result
     * @deprecated проверить на актуальность
     */
    @Deprecated(since = "2023-07-31")
    @PostMapping(value = {"/getRemainsPerPeriod", "/getRemainsPerPeriod/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Остатки за период", description = "Остатки за период")
    List<RemainsPerPeriodDTO> getRemainsPerPeriod(
            @Valid @RequestBody List<UUID> requestList,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                 );


    /**
     * Delete limits by organization.
     *
     * @return success status.
     */
    @DeleteMapping(value = {"/delete/{organizationId}", "/delete/{organizationId}/"})
    @ResponseBody
    @Operation(summary = "Удаление лимитов", description = "Удаление лимитов")
    Boolean deleteLimits(
            @PathVariable("organizationId") UUID organizationId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get limits by organization and year.
     *
     * @return success status.
     */
    @DeleteMapping(value = "/deleteByServiceTypeAndYear/{organizationId}/serviceType/{serviceType}/year/{year}")
    @ResponseBody
    @Operation(summary = "Удаление лимитов по году", description = "Удаление лимитов по году")
    Boolean deleteLimitsByServiceTypeAndYear(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("serviceType") String serviceType,
            @PathVariable("year") Integer year,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Gets limit children.
     *
     * @param limitId ID of limit.
     * @param limitType type of limit.
     * @param token authentication data.
     *
     * @return limit.
     * @deprecated use `GET /?parentLimitId={limitId}` instead
     */
    @Deprecated(since = "2023-07-31")
    @GetMapping(value = "/limitchildren/{limitId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение дочерних лимитов",
               description = "Получение дочерних лимитов")
    List<GetLimitDTO> getLimitChildren(@PathVariable("limitId") @NotNull UUID limitId,
                                       @RequestParam(value = "limitType", required = false) String limitType,
                                       JwtAuthenticationToken token);

    /**
     * Get limits audit.
     *
     * @param authentication authentication data.
     * @param year year of limits.
     * @param organizationId ID of organization.
     *
     * @return list of limits.
     */
    @GetMapping(value = "/audit/org/{organizationId}/year/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Аудит лимитов", description = "Аудит лимитов")
    List<String> auditLimits(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("year") Integer year,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Get limits audit.
     *
     * @param authentication authentication data.
     * @param year year of limits.
     * @param organizationId ID of organization.
     *
     * @return list of limits.
     */
    @GetMapping(value = "/auditAsync/org/{organizationId}/year/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Аудит лимитов асинхронный", description = "Аудит лимитов")
    TaskStartedDto auditLimitsAsync(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("year") Integer year,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Close limits audit.
     *
     * @param authentication authentication data.
     * @param limitId ID of limit.
     * @param organizationId ID of organization.
     * @param source source.
     *
     * @return list of limits.
     */
    @GetMapping(value = "/closeAsync/org/{organizationId}/limit/{limitId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Закрытие лимитов асинхронный", description = "Закрытие лимитов")
    TaskStartedDto closeLimitsAsync(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("limitId") UUID limitId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source);


    /**
     * Get limit by employee and date.
     *
     * @param employeeId employee id.
     * @param date date limit
     * @param transportType type of transport.
     *
     * @return limit dto.
     */
    @GetMapping(value = "/getLimitInfoDate/{employeeId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимита по сотруднику и дате", description = "Получение лимита по сотруднику и дате")
    GetLimitInfoDto getLimitInfoByDate(
            @PathVariable("employeeId") @NotNull UUID employeeId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("transportType") TransportTypeEnum transportType);
}
