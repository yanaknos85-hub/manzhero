package ru.sber.transport.fraud.monitoring.providers.trip_request.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.TripRequestRecord;
import ru.sber.transport.fraud.monitoring.model.*;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData.FraudMessageItem;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudModelWithMessages;
import ru.sber.transport.fraud.monitoring.providers.waypoint.model.WaypointDatabaseModel;

import java.util.*;

/**
 * Модель заявки на поездку.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class TripRequestDatabaseModelWithMessages implements TripRequestDataWithMessages {

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

    private List<FraudModelWithMessages> frauds;

    private Map<UUID, List<MessagingModel>> fraudMessages;

    @Override
    public List<FraudCaseData> getFrauds() {
        if (frauds == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(frauds);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<UUID, List<FraudMessageItem>> getFraudMessages() {
        if (fraudMessages == null) {
            return Collections.emptyMap();
        }
        return (Map) fraudMessages;
    }

    @Override
    public List<Waypoint> getWaypoints() {
        if (waypoints == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(waypoints);
    }
}
