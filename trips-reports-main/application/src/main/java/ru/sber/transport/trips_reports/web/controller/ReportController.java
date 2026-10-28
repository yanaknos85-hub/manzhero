package ru.sber.transport.trips_reports.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;

import java.util.List;
import java.util.UUID;

@RequestMapping("/")
public interface ReportController {

    @GetMapping(value = "/contractor/{contractorId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Отчеты", description = "Получение отчетов")
    Iterable<ReportsDto> getTrips(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            ReportSearchDto searchDto,
            @RequestParam(value = "statuses", required = false) List<TripStatus> statuses
    );

    @GetMapping(value = "/report/{reportId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Заявки", description = "Получение детальной информации об отчете")
    ReportsDto getTrip(
            @Parameter(description = "Идентификатор отчета")
            @PathVariable("reportId") UUID reportId);

}