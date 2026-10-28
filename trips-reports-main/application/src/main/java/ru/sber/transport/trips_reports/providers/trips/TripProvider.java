package ru.sber.transport.trips_reports.providers.trips;


import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.web.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripProvider {

    /**
     * Save report.
     */
    int save(ReportRecord report);

    /**
     * Get reports.
     *
     * @param filters filters for request
     * @return reports list
     */
    List<ReportRecord> findAllByFilters(TripsExportFiltersDTO filters);

    /**
     * Получить список отчетов.
     *
     * @param contractorId ID контрагента
     * @param searchData   данные фильтра.
     * @param statuses     список статусов
     * @return список отчетов
     */
    Iterable<Reports> findAll(UUID contractorId, ReportSearchDto searchData, List<TripStatus> statuses);

    /**
     * Получить отчет по ID и ID контрагента.
     *
     * @param reportId идентификатор отчета.
     * @return отчет.
     */
    Optional<Reports> findById(UUID reportId);

}
