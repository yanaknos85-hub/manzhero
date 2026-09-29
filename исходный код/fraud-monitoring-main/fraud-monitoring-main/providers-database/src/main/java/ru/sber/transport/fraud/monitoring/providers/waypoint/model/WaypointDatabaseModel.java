package ru.sber.transport.fraud.monitoring.providers.waypoint.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import ru.sber.transport.database.fraud_monitoring.tables.records.WaypointRecord;
import ru.sber.transport.fraud.monitoring.model.Waypoint;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс описывает модель точки маршрута.
 */
@RequiredArgsConstructor
@Getter
@Setter
public class WaypointDatabaseModel implements Waypoint {

    @Delegate
    private final WaypointRecord delegatee;

    @Override
    public String getAddress() {
        return Stream.of(
                        delegatee.getRegion(),
                        delegatee.getCity() != null
                                && !delegatee.getCity().equals(delegatee.getRegion())
                                ? delegatee.getCity() : null,
                        delegatee.getStreet(),
                        delegatee.getHouse()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}
