package ru.sber.transport.fraud.monitoring.providers.trip_request;

import jakarta.validation.constraints.NotNull;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.util.StringUtils;
import ru.sber.transport.database.fraud_monitoring.tables.TripRequest;
import ru.sber.transport.fraud.monitoring.model.RequestFilter;

import java.util.ArrayList;
import java.util.UUID;

import static ru.sber.transport.database.fraud_monitoring.Tables.*;

/**
 * Формирование запроса к БД
 */
class DataQuery {

    /**
     * Связь с сотрудником (пассажиром)
     */
    public static final Name PASSENGER = DSL.name("passenger");

    /**
     * Связь с сотрудником (согласующим)
     */
    public static final Name ACTUAL_APPROVER = DSL.name("actual_approver");

    private static final TripRequest table = TRIP_REQUEST;

    private final DSLContext context;
    private final RequestFilter filter;

    /**
     * Создание запроса к БД
     *
     * @param context сессия
     * @param filter  фильтр
     */
    DataQuery(DSLContext context, RequestFilter filter) {
        this.context = context;
        this.filter = filter;
    }

    /**
     * Получение запроса на данные
     *
     * @return запрос на данные
     */
    SelectConditionStep<Record> getDataQuery() {
        final var baseDataQuery = getBaseDataQuery();
        final var query = baseDataQuery.where(DSL.trueCondition());
        return appendFilters(query, filter);
    }

    /**
     * Получение запроса на количество
     *
     * @return запрос на количество
     */
    SelectConditionStep<Record> getCountQuery() {
        return appendFilters(getBaseCountQuery().where(DSL.trueCondition()), filter);
    }

    @NotNull
    private SelectOnConditionStep<Record> getBaseDataQuery() {
        final var fields = new ArrayList<SelectFieldOrAsterisk>();

        fields.add(TRIP_REQUEST.ID);
        fields.add(TRIP_REQUEST.HUMAN_READABLE_ID);
        fields.add(TRIP_REQUEST.DESIRED_DATE);
        fields.add(TRIP_REQUEST.APPROVAL_DATE);
        fields.add(TRIP_REQUEST.PASSENGER_ID);
        fields.add(TRIP_REQUEST.APPROVER_ID);
        fields.add(TRIP_REQUEST.PURPOSE_ID);
        fields.add(TRIP_REQUEST.REQUEST_STATUS);
        fields.add(TRIP_REQUEST.TRANSPORT_TYPE);
        fields.add(TRIP_REQUEST.PLANNED_COST);
        fields.add(TRIP_REQUEST.ACTUAL_COST);
        fields.add(TRIP_REQUEST.TIME_ZONE);
        fields.add(TRIP_REQUEST.DISTANCE);
        fields.add(TRIP_REQUEST.DEPARTMENT_ID);
        fields.add(TRIP_REQUEST.TARIFF);
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.ID.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName())));
        fields.add(DSL.field(PASSENGER.append(EMPLOYEE.PERSONNEL_NUMBER.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.COST_CENTER.getUnqualifiedName())));
        fields.add(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.PERSONNEL_NUMBER.getUnqualifiedName())));
        fields.add(TRIP_PURPOSE.ID);
        fields.add(TRIP_PURPOSE.LABEL);
        fields.add(DEPARTMENT.ID);
        fields.add(DEPARTMENT.NAME);
        fields.add(DEPARTMENT.CODE);

        return joins(context.selectDistinct(fields));
    }

    @NotNull
    private SelectOnConditionStep<Record> getBaseCountQuery() {
        final var countFields = new ArrayList<SelectField<?>>();

        countFields.add(DSL.countDistinct(TRIP_REQUEST.ID).as("trip_request_count"));

        return joins(context.select(countFields));
    }

    @NotNull
    private SelectOnConditionStep<Record> joins(SelectSelectStep<Record> query) {
        return query.from(table)
                .innerJoin(EMPLOYEE.as(PASSENGER)).on(table.PASSENGER_ID.eq(DSL.field(PASSENGER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class)))
                .innerJoin(TRIP_PURPOSE).on(table.PURPOSE_ID.eq(TRIP_PURPOSE.ID))
                .innerJoin(DEPARTMENT).on(DSL.field(PASSENGER.append(EMPLOYEE.DEPARTMENT_ID.getUnqualifiedName()), UUID.class).eq(DEPARTMENT.ID))
                .innerJoin(FRAUD).on(table.ID.eq(FRAUD.REQUEST_ID))
                .leftJoin(EMPLOYEE.as(ACTUAL_APPROVER)).on(DSL.field(TRIP_REQUEST.APPROVER_ID.eq(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.ID.getUnqualifiedName()), UUID.class))));
    }

    private <S extends Record> SelectConditionStep<S> appendFilters(SelectConditionStep<S> wheredQuery, RequestFilter filter) {

        final var passengerIds = filter.passenger();
        if (passengerIds != null && !passengerIds.isEmpty()) {
            wheredQuery = wheredQuery.and(table.PASSENGER_ID.in(passengerIds));
        }

        final var passengerName = filter.passengerName();
        if (StringUtils.hasText(passengerName)) {
            wheredQuery = wheredQuery.and(
                    DSL.field(PASSENGER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())).likeIgnoreCase("%" + passengerName + "%")
                            .or(DSL.field(PASSENGER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())).likeIgnoreCase("%" + passengerName + "%"))
                            .or(DSL.field(PASSENGER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())).likeIgnoreCase("%" + passengerName + "%"))
            );
        }

        final var approverIds = filter.approver();
        if (approverIds != null && !approverIds.isEmpty()) {
            wheredQuery = wheredQuery.and(table.APPROVER_ID.in(approverIds));
        }

        final var approverName = filter.approverName();
        if (StringUtils.hasText(approverName)) {
            wheredQuery = wheredQuery.and(
                    DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.LAST_NAME.getUnqualifiedName())).likeIgnoreCase("%" + approverName + "%")
                            .or(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.FIRST_NAME.getUnqualifiedName())).likeIgnoreCase("%" + approverName + "%"))
                            .or(DSL.field(ACTUAL_APPROVER.append(EMPLOYEE.PATRONYMIC.getUnqualifiedName())).likeIgnoreCase("%" + approverName + "%"))
            );
        }

        final var tripDateStart = filter.tripDateStart();
        if (tripDateStart != null) {
            wheredQuery = wheredQuery.and(table.DESIRED_DATE.ge(tripDateStart));
        }

        final var tripDateEnd = filter.tripDateEnd();
        if (tripDateEnd != null) {
            wheredQuery = wheredQuery.and(table.DESIRED_DATE.le(tripDateEnd));
        }

        final var approveDateStart = filter.approveDateStart();
        if (approveDateStart != null) {
            wheredQuery = wheredQuery.and(table.APPROVAL_DATE.ge(approveDateStart));
        }

        final var approveDateEnd = filter.approveDateEnd();
        if (approveDateEnd != null) {
            wheredQuery = wheredQuery.and(table.APPROVAL_DATE.le(approveDateEnd));
        }

        final var humanReadableId = filter.humanReadableId();
        if (StringUtils.hasText(humanReadableId)) {
            wheredQuery = wheredQuery.and(table.HUMAN_READABLE_ID.likeIgnoreCase("%" + humanReadableId + "%"));
        }

        final var transportType = filter.transportType();
        if (transportType != null && !transportType.isEmpty()) {
            wheredQuery = wheredQuery.and(table.TRANSPORT_TYPE.in(transportType));
        }

        final var purpose = filter.purpose();
        if (purpose != null && !purpose.isEmpty()) {
            wheredQuery = wheredQuery.and(table.PURPOSE_ID.in(purpose));
        }

        return wheredQuery;
    }
}
