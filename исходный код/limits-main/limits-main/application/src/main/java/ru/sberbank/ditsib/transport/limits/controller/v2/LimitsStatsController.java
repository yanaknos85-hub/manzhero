package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.GeneralAnalyticalReportResponseDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GeneralAnalyticalReportRequestV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;

import java.util.UUID;

@RequestMapping({"/statistic", "/statistic/"})
public interface LimitsStatsController {

    @GetMapping(value = {"/{organizationId}/department/{departmentId}", "/{organizationId}/department/{departmentId}/"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Экспорт статистики по лимитам", description = "Экспорт статистики по лимитам")
    LimitStatsV2DTO getLimitStats(@PathVariable("organizationId") UUID organizationId,
                                  @RequestParam(value = "year", required = false) Integer year,
                                  @RequestParam(value = "month", required = false) Month month,
                                  @RequestParam(value = "day", required = false) Integer day,
                                  @PathVariable(value = "departmentId") UUID departmentId);

    @GetMapping(value = {"/{organizationId}/department/{departmentId}", "/{organizationId}/department/{departmentId}/"},
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Экспорт статистики по лимитам", description = "Экспорт статистики по лимитам")
    InputStreamResource getLimitStatsBytes(@PathVariable("organizationId") UUID organizationId,
                              @RequestParam(value = "year", required = false) Integer year,
                              @RequestParam(value = "month", required = false) Month month,
                              @RequestParam(value = "day", required = false) Integer day,
                              @PathVariable(value = "departmentId") UUID departmentId);

    /**
     * Получение данных по общему аналитическому отчету V2
     *
     * @param request параметры запроса в виде JSON
     *                year - год, за который нужно получить данные
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportDataV2(
        @RequestBody GeneralAnalyticalReportRequestV2DTO request,
        @Parameter(hidden = true) JwtAuthenticationToken authentication);


    /**
     * Получение данных по общему аналитическому отчету
     *
     * @param request параметры запроса в виде JSON
     *                year - год, за который нужно получить данные
     * @deprecated эндпоинт устарел
     */
    @Deprecated(forRemoval = true)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GeneralAnalyticalReportResponseDTO getGeneralAnalyticalReportData(
            GeneralAnalyticalReportRequestV2DTO request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication);
}
