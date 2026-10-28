package ru.sber.transport.trips_reports.web.service;

import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.ReportSearchDto;
import ru.sber.transport.trips_reports.web.dto.TripStatus;

import java.util.List;
import java.util.UUID;

public interface ReportService {

    /**
     * Получение всех отчетов.
     *
     * @param contractorId идентификатор контрагента.
     * @param searchData   данные для фильтрации.
     * @return список отчетов.
     */
    Iterable<Reports> getAll(UUID contractorId, ReportSearchDto searchData, List<TripStatus> statuses);

    /**
     * Получение отчета.
     *
     * @param reportId     идентификатор отчета.
     * @return отчет.
     */
    Reports find(UUID reportId);

}
