package ru.sber.transport.trips_reports.web.service;

import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportsDto;

public interface ResultProcessor {

    Iterable<ReportsDto> process(Iterable<Reports> result);

    ReportsDto process(Reports result);

}
