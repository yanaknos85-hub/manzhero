package ru.sber.transport.trips_reports.providers.trips.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import ru.sber.transport.trips_reports.database.trips_reports.Keys;
import ru.sber.transport.trips_reports.database.trips_reports.Tables;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ReportRecord;
import ru.sber.transport.trips_reports.messaging.mapper.ReportMapper;
import ru.sber.transport.trips_reports.model.Reports;
import ru.sber.transport.trips_reports.providers.trips.TripProvider;
import ru.sber.transport.trips_reports.web.dto.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
class TripProviderImpl implements TripProvider {

    private final DSLContext dslContext;
    private final ReportMapper reportMapper;

    @Override
    public int save(ReportRecord report) {
        return dslContext.insertInto(Tables.REPORT).set(report)
                .onConflict(Keys.PK_REPORT.getFields()).doUpdate().set(report)
                .execute();
    }

    @Override
    public List<ReportRecord> findAllByFilters(TripsExportFiltersDTO filters) {
        if (filters == null || filters.getContractorId() == null) {
            return Collections.emptyList();
        }
        var select = dslContext.selectFrom(Tables.REPORT)
                .where(Tables.REPORT.CONTRACTOR_ID.eq(filters.getContractorId()));
        if (filters.getDuration() != null) {
            OffsetDateTime start = filters.getDuration().getStart();
            OffsetDateTime end = filters.getDuration().getEnd();

            if (start != null && end != null) {
                select = select.and(Tables.REPORT.FACT_START_TIME.between(start, end));
            } else if (start != null) {
                select = select.and(Tables.REPORT.FACT_START_TIME.greaterOrEqual(start));
            } else if (end != null) {
                select = select.and(Tables.REPORT.FACT_START_TIME.lessOrEqual(end));
            }
        }
        if (filters.getStatuses() != null) {
            select = select.and(Tables.REPORT.STATUS.in(filters.getStatuses().stream()
                    .map(TripStatus::getRusName)
                    .collect(Collectors.toList())));
        }
        return select.orderBy(Tables.REPORT.CREATION_TIME.desc())
                .fetchInto(ReportRecord.class);
    }

    @Override
    public Optional<Reports> findById(UUID reportId) {
        return dslContext.selectFrom(Tables.REPORT)
                .where(Tables.REPORT.ID.eq(reportId))
                .fetchOptional().map(reportMapper::toModel);
    }

    @Override
    public Iterable<Reports> findAll(UUID contractorId, ReportSearchDto searchData, List<TripStatus> statuses) {
        var stringStatuses = statuses.stream().map(TripStatus::getRusName).toList();
        var select = dslContext.selectFrom(Tables.REPORT)
                .where(Tables.REPORT.CONTRACTOR_ID.eq(contractorId))
                .and(Tables.REPORT.STATUS.in(stringStatuses));
        return enrichRequestBySearchData(select, searchData);
    }

    private Iterable<Reports> enrichRequestBySearchData(SelectConditionStep<ReportRecord> request, ReportSearchDto searchData) {
        var requestAppended = appendFilters(searchData, request);
        var pageNumber = searchData.getPage();
        var pageSize = searchData.getSize();
        var page = PageRequest.of(pageNumber, pageSize);
        var total = dslContext.fetchCount(requestAppended);
        var sort = searchData.getField().getSortField().sort(SortOrder.valueOf(searchData.getDirection().name()));
        var list = requestAppended
                .orderBy(sort)
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetchInto(ReportRecord.class);
        return new PageImpl<>(
                list.stream().map(reportMapper::toModel).collect(Collectors.toList()),
                page,
                total
        );
    }

    private <T extends Record> SelectConditionStep<T> appendFilters(ReportSearchDto searchData, SelectConditionStep<T> select) {
        var filter = searchData.getFilter();
        for (var entry : filter.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value != null) {
                switch (key) {
                    case REQUEST_HUMAN_READABLE_ID -> select = appendRequestHumanReadableId(value, select);
                    case VEHICLE_NUMBER -> select = appendVehicleNumber(value, select);
                    case START_TIME_FROM -> select = appendStartTimeFrom(value, select);
                    case START_TIME_TO -> select = appendStartTimeTo(value, select);
                    default -> {
                        throw new IllegalArgumentException("Неизвестный ключ фильтра: " + key);
                    }
                }
            }
        }
        return select;
    }

    private <T extends Record> SelectConditionStep<T> appendRequestHumanReadableId(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.REPORT.REQUEST_IDS.like("%" + value + "%"));
    }

    private <T extends Record> SelectConditionStep<T> appendVehicleNumber(Serializable value, SelectConditionStep<T> select) {
        return select.and(Tables.REPORT.VEHICLE_NUMBER.likeIgnoreCase("%" + value + "%"));
    }

    private <T extends Record> SelectConditionStep<T> appendStartTimeFrom(Serializable startTimeFrom, SelectConditionStep<T> select) {
        return select.and(Tables.REPORT.FACT_START_TIME.greaterOrEqual(ZonedDateTime.parse(String.valueOf(startTimeFrom)).toOffsetDateTime()));
    }

    private <T extends Record> SelectConditionStep<T> appendStartTimeTo(Serializable startTimeTo, SelectConditionStep<T> select) {
        return select.and(Tables.REPORT.FACT_START_TIME.lessOrEqual(ZonedDateTime.parse(String.valueOf(startTimeTo)).toOffsetDateTime()));
    }

}
