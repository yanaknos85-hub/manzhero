package ru.sber.transport.trips_reports.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.trips_reports.web.controller.ReportController;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;
import ru.sber.transport.trips_reports.web.service.ReportService;
import ru.sber.transport.trips_reports.web.service.ResultProcessor;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportControllerImpl implements ReportController {

    private final ReportService reportService;

    private final ResultProcessor resultProcessor;

    @Override
    public Iterable<ReportsDto> getTrips(UUID contractorId, ReportSearchDto searchDto, List<TripStatus> statuses) {
            var result = reportService.getAll(contractorId, searchDto, statuses);
            return resultProcessor.process(result);
    }

    @Override
    public ReportsDto getTrip(UUID reportId) {
        var result = reportService.find(reportId);
        return resultProcessor.process(result);
    }

}