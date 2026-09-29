package ru.sber.transport.fraud.monitoring.providers.trip_request.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.TripRequestRecord;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudModel;
import ru.sber.transport.fraud.monitoring.providers.waypoint.model.WaypointDatabaseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Модель заявки на поездку.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class TripRequestDatabaseModel implements TripRequestData {

    @Delegate
    private final TripRequestRecord delegatee;

    private Employee passenger;

    private Employee approver;

    private String costCenter;

    private TripPurpose purpose;

    private String departureAddress;

    private String destinationAddress;

    private Department department;

    private List<WaypointDatabaseModel> waypoints;

    private List<FraudModel> frauds;

    @Override
    public List<Waypoint> getWaypoints() {
        if (waypoints == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(waypoints);
    }

    @Override
    public List<Fraud> getFrauds() {
        if (frauds == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(frauds);
        }
    }
}
