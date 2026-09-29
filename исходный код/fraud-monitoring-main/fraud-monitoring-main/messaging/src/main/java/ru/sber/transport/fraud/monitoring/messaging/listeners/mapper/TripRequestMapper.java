package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.TripRequestImpl;
import ru.sber.transport.messages.request.external.avro.PlannedData;
import ru.sber.transport.messages.request.external.avro.RequestMessage;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Optional;

@Mapper(imports = ZoneOffset.class)
public interface TripRequestMapper {

    @Mapping(target = "requestStatus", source = "status")
    @Mapping(target = "transportType", constant = "YANDEX")
    @Mapping(target = "plannedCost", source = "planned.cost")
    @Mapping(target = "actualCost", source = "actual.cost")
    @Mapping(target = "approvalDate", expression = "java(message.getApprovalDate() != null ? message.getApprovalDate().atOffset(ZoneOffset.UTC) : null)")
    @Mapping(target = "desiredDate", expression = "java(message.getDate().atOffset(ZoneOffset.UTC))")
    @Mapping(target = "distance", expression = "java(getDistance(message))")
    @Mapping(target = "duration", expression = "java(getDuration(message))")
    @Mapping(target = "waypoints", ignore = true)
    TripRequestImpl externalRequestMessageToTripRequest(ExternalRequestMessage message);

    @Mapping(target = "requestStatus", source = "status")
    @Mapping(target = "transportType", constant = "YANDEX")
    @Mapping(target = "plannedCost", source = "planned.cost")
    @Mapping(target = "actualCost", source = "actual.cost")
    @Mapping(target = "approvalDate", expression = "java(message.getApprovalDate() != null ? message.getApprovalDate().atOffset(ZoneOffset.UTC) : null)")
    @Mapping(target = "desiredDate", expression = "java(message.getDate().atOffset(ZoneOffset.UTC))")
    @Mapping(target = "distance", expression = "java(getDistance(message))")
    @Mapping(target = "duration", expression = "java(getDuration(message))")
    @Mapping(target = "waypoints", ignore = true)
    TripRequestImpl externalRequestMessageToTripRequest(RequestMessage message);

    default Double getDistance(ExternalRequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
                .map(ExternalRequestMessage.PlannedData::getDistance)
                .map(distance -> distance / 1000.0)
                .orElse(null);
    }

    default Long getDuration(ExternalRequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
                .map(ExternalRequestMessage.PlannedData::getDuration)
                .map(Duration::parse)
                .map(Duration::toSeconds)
                .orElse(null);
    }

    default Double getDistance(RequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
                .map(PlannedData::getDistance)
                .map(distance -> distance / 1000.0)
                .orElse(null);
    }

    default Long getDuration(RequestMessage message) {
        return Optional.ofNullable(message.getPlanned())
                .map(PlannedData::getDuration)
                .map(Duration::parse)
                .map(Duration::toSeconds)
                .orElse(null);
    }
}
