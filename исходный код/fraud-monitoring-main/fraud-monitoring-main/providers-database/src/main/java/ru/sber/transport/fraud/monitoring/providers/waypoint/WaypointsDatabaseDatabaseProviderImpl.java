package ru.sber.transport.fraud.monitoring.providers.waypoint;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.Waypoint;
import ru.sber.transport.database.fraud_monitoring.tables.records.WaypointRecord;
import ru.sber.transport.fraud.monitoring.providers.WaypointsDatabaseProvider;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация провайдера путевых точек
 */
@Transactional
@RequiredArgsConstructor
public class WaypointsDatabaseDatabaseProviderImpl implements WaypointsDatabaseProvider, JooqRepository<Waypoint, WaypointRecord, UUID> {

    @Override
    public Waypoint table() {
        return Tables.WAYPOINT;
    }

    @Override
    public ru.sber.transport.fraud.monitoring.model.Waypoint save(ru.sber.transport.fraud.monitoring.model.Waypoint source) {
        final var waypointRecord = context().insertInto(table())
                .set(table().ID, source.getId())
                .set(table().TRIP_REQUEST_ID, source.getTripRequestId())
                .set(table().COUNTRY, source.getCountry())
                .set(table().REGION, source.getRegion())
                .set(table().CITY, source.getCity())
                .set(table().STREET, source.getStreet())
                .set(table().HOUSE, source.getHouse())
                .set(table().STRUCTURE, source.getStructure())
                .set(table().BUILDING, source.getBuilding())
                .set(table().ORDERING_INDEX, source.getOrderingIndex())
                .set(table().WAIT_TIME, source.getWaitTime())
                .onConflict(table().ID)
                .doUpdate()
                .set(table().TRIP_REQUEST_ID, source.getTripRequestId())
                .set(table().COUNTRY, source.getCountry())
                .set(table().REGION, source.getRegion())
                .set(table().CITY, source.getCity())
                .set(table().STREET, source.getStreet())
                .set(table().HOUSE, source.getHouse())
                .set(table().STRUCTURE, source.getStructure())
                .set(table().BUILDING, source.getBuilding())
                .set(table().ORDERING_INDEX, source.getOrderingIndex())
                .set(table().WAIT_TIME, source.getWaitTime())
                .returning()
                .fetchOne();

        return createWaypoint(waypointRecord);
    }

    private ru.sber.transport.fraud.monitoring.model.Waypoint createWaypoint(WaypointRecord waypointRecord) {
        return new ru.sber.transport.fraud.monitoring.model.Waypoint() {

            @Override
            public UUID getId() {
                return waypointRecord.getId();
            }

            @Override
            public UUID getTripRequestId() {
                return waypointRecord.getTripRequestId();
            }

            @Override
            public String getCountry() {
                return waypointRecord.getCountry();
            }

            @Override
            public String getRegion() {
                return waypointRecord.getRegion();
            }

            @Override
            public String getCity() {
                return waypointRecord.getCity();
            }

            @Override
            public String getStreet() {
                return waypointRecord.getStreet();
            }

            @Override
            public String getHouse() {
                return waypointRecord.getHouse();
            }

            @Override
            public String getStructure() {
                return waypointRecord.getStructure();
            }

            @Override
            public String getBuilding() {
                return waypointRecord.getBuilding();
            }

            @Override
            public Integer getOrderingIndex() {
                return waypointRecord.getOrderingIndex();
            }

            @Override
            public Long getWaitTime() {
                return waypointRecord.getWaitTime();
            }

            @Override
            public String getAddress() {
                return Stream.of(
                                waypointRecord.getRegion(),
                                waypointRecord.getCity() != null
                                        && !waypointRecord.getCity().equals(waypointRecord.getRegion())
                                        ? waypointRecord.getCity() : null,
                                waypointRecord.getStreet(),
                                waypointRecord.getHouse()
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "));
            }
        };
    }
}
