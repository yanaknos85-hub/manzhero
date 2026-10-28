package ru.sber.transport.trips_reports.web.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.TableField;
import ru.sber.transport.trips_reports.database.trips_reports.Tables;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sberbank.ditsib.request.SortField;

/**
 * Параметры поиска отчетов.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ReportSearchParameters implements SortField {

    /**
     * Читаемый идентификатор отчета.
     */
    HUMAN_READABLE_ID("humanReadableId", "digitId", Tables.REPORT.ID),

    /**
     * Читаемый идентификатор запроса.
     */
    REQUEST_HUMAN_READABLE_ID("requestHumanReadableId", "request.humanReadableId", Tables.REPORT.REQUEST_IDS),

    /**
     * Номер транспортного средства.
     */
    VEHICLE_NUMBER("vehicleNumber", null, Tables.REPORT.VEHICLE_NUMBER),

    /**
     * Время начала поездки.
     */
    DESIRED_DATE("desiredDate", "start_time", Tables.REPORT.EXPECTED_START_TIME),

    /**
     * Начальная дата периода.
     */
    START_TIME_FROM("startTimeFrom", null, null),

    /**
     * Конечная дата периода.
     */
    START_TIME_TO("startTimeTo", null, null);

    /**
     * Имя параметра.
     */
    private final String name;

    /**
     * Имя поля для сортировки.
     */
    private final String sortFieldName;

    /**
     * Поле сортировки в таблице.
     */
    private final TableField<ReportRecord, ?> sortField;
}