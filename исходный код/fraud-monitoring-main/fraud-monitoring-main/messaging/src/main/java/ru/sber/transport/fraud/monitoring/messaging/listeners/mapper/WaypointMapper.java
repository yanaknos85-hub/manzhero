package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.WaypointImpl;
import ru.sber.transport.messages.request.external.avro.WaypointMessage;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper
public interface WaypointMapper {

    @Mapping(target = "waitTime", constant = "0L")
    @Mapping(target = "address", expression = "java(getAddress(waypointMessage))")
    WaypointImpl waypointMessageToWaypointImpl(ExternalRequestMessage.WaypointMessage waypointMessage,
                                               int orderingIndex, UUID tripRequestId);

    @Mapping(target = "waitTime", constant = "0L")
    @Mapping(target = "address", expression = "java(getAddress(waypointMessage))")
    WaypointImpl waypointMessageToWaypointImpl(WaypointMessage waypointMessage,
                                                   int orderingIndex, UUID tripRequestId);

    default String getAddress(ExternalRequestMessage.WaypointMessage waypoint) {
        return Stream.of(
                        waypoint.getRegion(),
                        waypoint.getCity() != null
                                && !waypoint.getCity().equals(waypoint.getRegion())
                                ? waypoint.getCity() : null,
                        waypoint.getStreet(),
                        waypoint.getHouse()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    default String getAddress(WaypointMessage waypoint) {
        return Stream.of(
                        waypoint.getRegion(),
                        waypoint.getCity() != null
                                && !waypoint.getCity().equals(waypoint.getRegion())
                                ? waypoint.getCity() : null,
                        waypoint.getStreet(),
                        waypoint.getHouse()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}
