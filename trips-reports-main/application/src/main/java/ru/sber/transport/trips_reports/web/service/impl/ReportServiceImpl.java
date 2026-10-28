package ru.sber.transport.trips_reports.web.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;
import ru.sber.transport.trips_reports.web.service.ReportService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {

    private final TripProvider tripProvider;

    @Override
    public Iterable<Reports> getAll(UUID contractorId, ReportSearchDto searchData, List<TripStatus> statuses) {
        return tripProvider.findAll(contractorId, searchData, statuses);
    }

    @Override
    public Reports find(UUID reportId) {
        return tripProvider.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException(ReportsDto.class, reportId));
    }

}