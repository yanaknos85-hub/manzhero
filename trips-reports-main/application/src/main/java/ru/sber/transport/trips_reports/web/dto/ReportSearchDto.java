package ru.sber.transport.trips_reports.web.dto;

import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.time.OffsetDateTime;

/**
 * Объект поиска данных об отчетах.
 */
@Setter
@Getter
public class ReportSearchDto extends PageSortFilterParameters<ReportSearchParameters> {

    /**
     * Читаемый идентификатор запроса для поиска.
     */
    private String requestHumanReadableId;

    /**
     * Читаемый идентификатор поездки для поиска.
     */
    private String tripHumanReadableId;

    /**
     * Номер транспортного средства для поиска.
     */
    private String vehicleNumber;

    /**
     * Время начала отчетов, начиная с которого ищется информация.
     */
    private OffsetDateTime startTimeFrom;

    /**
     * Время начала отчетов, до которого ищется информация.
     */
    private OffsetDateTime startTimeTo;

    /**
     * Конструктор по умолчанию.
     * Инициализирует объект с параметром сортировки по умолчанию.
     */
    public ReportSearchDto() {
        super(ReportSearchParameters.HUMAN_READABLE_ID);
    }
}