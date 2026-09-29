package ru.sber.transport.fraud.monitoring.providers.trip_request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.records.*;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.WaypointsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.department.DepartmentModel;
import ru.sber.transport.fraud.monitoring.providers.employees.EmployeeModel;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudModel;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudModelWithMessages;
import ru.sber.transport.fraud.monitoring.providers.trip_purpose.model.TripPurposeModel;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.DatabasePage;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.MessagingModel;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.TripRequestDatabaseModel;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.TripRequestDatabaseModelWithMessages;
import ru.sber.transport.fraud.monitoring.providers.waypoint.model.WaypointDatabaseModel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static ru.sber.transport.database.fraud_monitoring.Tables.*;
import static ru.sber.transport.fraud.monitoring.providers.trip_request.DataQuery.ACTUAL_APPROVER;
import static ru.sber.transport.fraud.monitoring.providers.trip_request.DataQuery.PASSENGER;

/**
 * Реализация провайдера заявок на поездки
 */
@Slf4j
@RequiredArgsConstructor
public class TripRequestsDatabaseDatabaseProviderImpl implements TripRequestsDatabaseProvider, JooqRepository<ru.sber.transport.database.fraud_monitoring.tables.TripRequest, TripRequestRecord, UUID> {

    private final WaypointsDatabaseProvider waypointsDatabaseProvider;

    @Override
    public ru.sber.transport.database.fraud_monitoring.tables.TripRequest table() {
        return Tables.TRIP_REQUEST;
    }

    @Override
    @Transactional
    public TripRequest createOrUpdate(TripRequest source) {
        if (source == null) {
            return null;
        }
        final var tripRequestRecord = findById(source.getId()).orElseGet(TripRequestRecord::new);
        tripRequestRecord.setId(source.getId());

        tripRequestRecord.setTransportType(source.getTransportType().name());
        tripRequestRecord.setTariff(source.getTariff());
        tripRequestRecord.setDesiredDate(source.getDesiredDate());
        tripRequestRecord.setTimeZone(source.getTimeZone());
        tripRequestRecord.setOrganizationId(source.getOrganizationId());
        tripRequestRecord.setDepartmentId(source.getDepartmentId());
        tripRequestRecord.setPurposeId(source.getPurposeId());
        tripRequestRecord.setPassengerId(source.getPassengerId());
        tripRequestRecord.setApproverId(source.getApproverId());
        tripRequestRecord.setActualCost(source.getActualCost());
        tripRequestRecord.setPlannedCost(source.getPlannedCost());
        tripRequestRecord.setRequestStatus(source.getRequestStatus());
        tripRequestRecord.setApprovalDate(source.getApprovalDate());
        tripRequestRecord.setHumanReadableId(source.getHumanReadableId());
        tripRequestRecord.setDistance(source.getDistance());
        tripRequestRecord.setCompensationType(source.getCompensationType());
        tripRequestRecord.setDuration(source.getDuration());

        for (int i = 0; i < source.getWaypoints().size(); i++) {
            waypointsDatabaseProvider.save(source.getWaypoints().get(i));
        }

        final var saved = save(tripRequestRecord);
        log.debug("Trip request successfully saved: {}", source.getId());
        return createTripRequest(saved);
    }

    @Override
    public boolean exists(UUID id) {
        return existsById(id);
    }

    @Override
    @Transactional
    public boolean createIfNotExists(UUID id) {
        if (existsById(id)) {
            return false;
        }

        final var tripRequestRecord = context().newRecord(Tables.TRIP_REQUEST);
        tripRequestRecord.setId(id);
        save(tripRequestRecord);
        log.debug("Trip request created with id: {}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TripRequestData> get(UUID id) {
        final var approverAlias = EMPLOYEE.as("approver");

        Optional<TripRequestData> tripRequestOpt = context().select()
                .from(table())
                .innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID))
                .innerJoin(DEPARTMENT).on(EMPLOYEE.DEPARTMENT_ID.eq(DEPARTMENT.ID))
                .innerJoin(TRIP_PURPOSE).on(table().PURPOSE_ID.eq(TRIP_PURPOSE.ID))
                .leftJoin(approverAlias).on(table().APPROVER_ID.eq(approverAlias.ID))
                .where(table().ID.eq(id))
                .fetchOptional(element -> {
                    final var model = new TripRequestDatabaseModel(element.into(TripRequestRecord.class));
                    model.setPassenger(new EmployeeModel(element.into(EmployeeRecord.class)));
                    if (element.get(table().APPROVER_ID) != null) {
                        var approverRecord = element.into(approverAlias);
                        model.setApprover(new EmployeeModel(approverRecord));
                    } else {
                        model.setApprover(null);
                    }
                    model.setPurpose(new TripPurposeModel(element.into(TripPurposeRecord.class)));
                    model.setDepartment(new DepartmentModel(element.into(DepartmentRecord.class)));
                    model.setCostCenter(element.get(EMPLOYEE.COST_CENTER));
                    return model;
                });

        tripRequestOpt.ifPresent(tripRequest -> {
            var waypointDatabaseModelList = context().select()
                    .from(WAYPOINT)
                    .where(WAYPOINT.TRIP_REQUEST_ID.eq(id))
                    .orderBy(WAYPOINT.ORDERING_INDEX.asc())
                    .fetch(waypointRecord -> new WaypointDatabaseModel(waypointRecord.into(WaypointRecord.class)));

            var fraudModelList = context().select()
                    .from(FRAUD)
                    .where(FRAUD.REQUEST_ID.eq(id))
                    .fetch()
                    .stream()
                    .map(fraudRecord -> new FraudModel(fraudRecord.into(FraudRecord.class)))
                    .toList();

            var model = (TripRequestDatabaseModel) tripRequest;
            if (!waypointDatabaseModelList.isEmpty()) {
                model.setWaypoints(waypointDatabaseModelList);
                model.setDepartureAddress(toAddress(waypointDatabaseModelList.getFirst()));
                model.setDestinationAddress(toAddress(waypointDatabaseModelList.getLast()));
            }

            model.setFrauds(fraudModelList);
        });

        return tripRequestOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripRequestData> get(RequestFilter filter, int page, int size, String sort, boolean asc) {
        return executeQuery(new DataQuery(context(), filter), page, size, sort, asc, (target, source) -> {
            target.setPassenger(new EmployeeModel(PASSENGER, source));
            target.setPurpose(new TripPurposeModel(source.into(TripPurposeRecord.class)));
            target.setDepartment(new DepartmentModel(source.into(DepartmentRecord.class)));
            target.setApprover(new EmployeeModel(ACTUAL_APPROVER, source));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TripRequestDataWithMessages> getWithMessages(UUID id) {
        log.debug("Получение заявки на поездку с: {}", id);

        final var approverAlias = EMPLOYEE.as("approver");

        Optional<TripRequestDataWithMessages> tripRequestOpt = context().select()
                .from(table())
                .innerJoin(EMPLOYEE).on(table().PASSENGER_ID.eq(EMPLOYEE.ID))
                .innerJoin(DEPARTMENT).on(EMPLOYEE.DEPARTMENT_ID.eq(DEPARTMENT.ID))
                .innerJoin(TRIP_PURPOSE).on(table().PURPOSE_ID.eq(TRIP_PURPOSE.ID))
                .leftJoin(approverAlias).on(table().APPROVER_ID.eq(approverAlias.ID))
                .where(table().ID.eq(id))
                .fetchOptional(element -> {
                    final var model = new TripRequestDatabaseModelWithMessages(element.into(TripRequestRecord.class));
                    model.setPassenger(new EmployeeModel(element.into(EmployeeRecord.class)));
                    if (element.get(table().APPROVER_ID) != null) {
                        var approverRecord = element.into(approverAlias);
                        model.setApprover(new EmployeeModel(approverRecord));
                    } else {
                        model.setApprover(null);
                    }
                    model.setPurpose(new TripPurposeModel(element.into(TripPurposeRecord.class)));
                    model.setDepartment(new DepartmentModel(element.into(DepartmentRecord.class)));
                    model.setCostCenter(element.get(EMPLOYEE.COST_CENTER));
                    return model;
                });

        tripRequestOpt.ifPresent(tripRequest -> {
            var waypointDatabaseModelList = context().select()
                    .from(WAYPOINT)
                    .where(WAYPOINT.TRIP_REQUEST_ID.eq(id))
                    .orderBy(WAYPOINT.ORDERING_INDEX.asc())
                    .fetch(waypointRecord -> new WaypointDatabaseModel(waypointRecord.into(WaypointRecord.class)));

            var fraudModelList = context().select()
                    .from(FRAUD)
                    .where(FRAUD.REQUEST_ID.eq(id))
                    .fetch()
                    .stream()
                    .map(fraudRecord -> new FraudModelWithMessages(fraudRecord.into(FraudRecord.class)))
                    .toList();
            Map<UUID, List<MessagingModel>> fraudMessages = null;
            if (!fraudModelList.isEmpty()) {
                fraudMessages = context().select()
                        .from(MESSAGING)
                        .where(MESSAGING.FRAUD_CASE_ID.in(fraudModelList.stream()
                                .map(FraudModelWithMessages::getId)
                                .toList()))
                        .orderBy(MESSAGING.MESSAGE_DATE)
                        .fetch()
                        .stream()
                        .map(messagingRecord ->
                                new MessagingModel(messagingRecord.into(MessagingRecord.class)))
                        .collect(Collectors.groupingBy(MessagingModel::getFraudCaseId));

            }
            var model = (TripRequestDatabaseModelWithMessages) tripRequest;
            if (!waypointDatabaseModelList.isEmpty()) {
                model.setWaypoints(waypointDatabaseModelList);
                model.setDepartureAddress(toAddress(waypointDatabaseModelList.getFirst()));
                model.setDestinationAddress(toAddress(waypointDatabaseModelList.getLast()));
            }
            model.setFrauds(fraudModelList);
            model.setFraudMessages(fraudMessages != null ? fraudMessages : Collections.emptyMap());
        });

        if (tripRequestOpt.isEmpty()) {
            log.warn("Заявка на поездку с: {}  не найдена", id);
        }

        return tripRequestOpt;
    }

    @NotNull
    private Page<TripRequestData> executeQuery(DataQuery query, int page, int size, String sort, boolean asc, BiConsumer<TripRequestDatabaseModel, Record> mapper) {
        final var content = size > 0 ? requestContent(query, page, size, sort, asc, mapper) : List.<TripRequestDatabaseModel>of();

        if (!content.isEmpty()) {
            loadAdditionalDataForAll(content);
        }

        final var total = Objects.requireNonNull(query.getCountQuery().fetchOne()).get("trip_request_count", Integer.class);

        return new DatabasePage(content.stream().map(TripRequestData.class::cast).toList(), page, size, total, asc, sort);
    }

    private void loadAdditionalDataForAll(List<TripRequestDatabaseModel> tripRequests) {
        if (tripRequests.isEmpty()) {
            return;
        }

        var tripRequestIds = tripRequests.stream()
                .map(TripRequestData::getId)
                .toList();

        var waypointsMap = fetchWaypointsForIds(tripRequestIds);

        var fraudsMap = fetchFraudsForIds(tripRequestIds);

        for (TripRequestDatabaseModel tripRequest : tripRequests) {
            var id = tripRequest.getId();
            var waypointDatabaseModelList = waypointsMap.getOrDefault(id, Collections.emptyList());
            var fraudModelList = fraudsMap.getOrDefault(id, Collections.emptyList());

            tripRequest.setWaypoints(waypointDatabaseModelList);
            tripRequest.setFrauds(fraudModelList);

            if (!waypointDatabaseModelList.isEmpty()) {
                tripRequest.setDepartureAddress(toAddress(waypointDatabaseModelList.getFirst()));
                tripRequest.setDestinationAddress(toAddress(waypointDatabaseModelList.getLast()));
            }
        }
    }

    private Map<UUID, List<WaypointDatabaseModel>> fetchWaypointsForIds(List<UUID> tripRequestIds) {
        return context().select()
                .from(WAYPOINT)
                .where(WAYPOINT.TRIP_REQUEST_ID.in(tripRequestIds))
                .orderBy(WAYPOINT.TRIP_REQUEST_ID, WAYPOINT.ORDERING_INDEX.asc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        waypointRecord -> waypointRecord.get(WAYPOINT.TRIP_REQUEST_ID),
                        Collectors.mapping(
                                waypointRecord -> new WaypointDatabaseModel(waypointRecord.into(WaypointRecord.class)),
                                Collectors.toList()
                        )
                ));
    }

    private Map<UUID, List<FraudModel>> fetchFraudsForIds(List<UUID> tripRequestIds) {
        return context().select()
                .from(FRAUD)
                .where(FRAUD.REQUEST_ID.in(tripRequestIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        fraudRecord -> fraudRecord.get(FRAUD.REQUEST_ID),
                        Collectors.mapping(
                                fraudRecord -> new FraudModel(fraudRecord.into(FraudRecord.class)),
                                Collectors.toList()
                        )
                ));
    }

    @NotNull
    private List<TripRequestDatabaseModel> requestContent(DataQuery query, int page, int size, String sort, boolean asc, BiConsumer<TripRequestDatabaseModel, Record> mapper) {
        var baseQuery = query.getDataQuery();

        var sortField = getSortField(sort);
        if (sortField == null) {
            sortField = TRIP_REQUEST.HUMAN_READABLE_ID;
        }

        var orderField = asc ? sortField.asc() : sortField.desc();

        var result = baseQuery
                .orderBy(orderField)
                .limit(size)
                .offset(page * size)
                .fetch();

        return result
                .map(it -> {
                    String humanReadableId = it.get(TRIP_REQUEST.HUMAN_READABLE_ID);
                    return new AbstractMap.SimpleEntry<>(humanReadableId, it);
                })
                .stream()
                .map(it -> {
                    final var resultModel = new TripRequestDatabaseModel(it.getValue().into(TripRequestRecord.class));
                    mapper.accept(resultModel, it.getValue());
                    return resultModel;
                })
                .toList();
    }

    private Field<?> getSortField(String sortProperty) {
        return switch (sortProperty) {
            case "humanReadableId" -> TRIP_REQUEST.HUMAN_READABLE_ID;
            case "desiredDate" -> TRIP_REQUEST.DESIRED_DATE;
            case "approvalDate" -> TRIP_REQUEST.APPROVAL_DATE;
            case "status" -> TRIP_REQUEST.REQUEST_STATUS;
            case "transportType" -> TRIP_REQUEST.TRANSPORT_TYPE;
            default -> null;
        };
    }

    private String toAddress(WaypointDatabaseModel waypoint) {
        return (waypoint.getRegion() != null ? waypoint.getRegion() + ", " : "") +
                ((waypoint.getRegion() != null && waypoint.getCity() != null && !waypoint.getRegion().equals(waypoint.getCity())
                        || waypoint.getRegion() == null && waypoint.getCity() != null) ? waypoint.getCity() + ", " : "") +
                (waypoint.getStreet() != null ? waypoint.getStreet() + ", " : "") +
                (waypoint.getHouse() != null ? waypoint.getHouse() + ", " : "");
    }

    private TripRequest createTripRequest(@NotNull TripRequestRecord tripRequestRecord) {
        return new TripRequest() {

            @Override
            public UUID getId() {
                return tripRequestRecord.getId();
            }

            @Override
            public String getHumanReadableId() {
                return tripRequestRecord.getHumanReadableId();
            }

            @Override
            public TransportType getTransportType() {
                return TransportType.valueOf(tripRequestRecord.getTransportType());
            }

            @Override
            public String getTariff() {
                return tripRequestRecord.getTariff();
            }

            @Override
            public UUID getPassengerId() {
                return tripRequestRecord.getPassengerId();
            }

            @Override
            public UUID getApproverId() {
                return tripRequestRecord.getApproverId();
            }

            @Override
            public OffsetDateTime getDesiredDate() {
                return tripRequestRecord.getDesiredDate();
            }

            @Override
            public OffsetDateTime getApprovalDate() {
                return tripRequestRecord.getApprovalDate();
            }

            @Override
            public BigDecimal getPlannedCost() {
                return tripRequestRecord.getPlannedCost();
            }

            @Override
            public BigDecimal getActualCost() {
                return tripRequestRecord.getActualCost();
            }

            @Override
            public UUID getOrganizationId() {
                return tripRequestRecord.getOrganizationId();
            }

            @Override
            public UUID getDepartmentId() {
                return tripRequestRecord.getDepartmentId();
            }

            @Override
            public String getRequestStatus() {
                return tripRequestRecord.getRequestStatus();
            }

            @Override
            public UUID getPurposeId() {
                return tripRequestRecord.getPurposeId();
            }

            @Override
            public String getTimeZone() {
                return tripRequestRecord.getTimeZone();
            }

            @Override
            public List<Waypoint> getWaypoints() {
                return List.of();
            }

            @Override
            public Double getDistance() {
                return tripRequestRecord.getDistance();
            }

            @Override
            public String getCompensationType() {
                return tripRequestRecord.getCompensationType();
            }

            @Override
            public Long getDuration() {
                return tripRequestRecord.getDuration();
            }
        };
    }
}
