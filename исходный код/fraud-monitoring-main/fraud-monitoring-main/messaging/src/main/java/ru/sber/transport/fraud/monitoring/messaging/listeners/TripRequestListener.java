package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.model.TransportType;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.fraud.monitoring.model.Waypoint;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.AddressMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.sber.transport.fraud.monitoring.model.TransportType.*;


@Slf4j
@RequiredArgsConstructor
public class TripRequestListener implements Consumer<Message<RequestMessage>> {

    private static final Set<String> VALID_TRANSPORT_TYPES = Set.of(
            PUBLIC.name(),
            PERSONAL.name(),
            CARSHARING.name(),
            TAXI.name()
    );
    private final TripRequestsService tripRequestsService;

    @Override
    public void accept(Message<RequestMessage> requestMessage) {
        final var payload = requestMessage.getPayload();

        log.debug("Received trip request: {}", payload);
        if (isValidTripRequest(payload)) {
            final var tripRequest = createTripRequestFromPayload(payload);
            tripRequestsService.createOrUpdate(tripRequest);
            log.debug("Trip request saved successfully: {}", tripRequest.getId());
        }
    }

    private boolean isValidTripRequest(RequestMessage payload) {
        if (Objects.isNull(payload) || Objects.isNull(payload.getTransportType())) {
            return false;
        }

        return VALID_TRANSPORT_TYPES.contains(payload.getTransportType().toUpperCase());
    }

    private TripRequest createTripRequestFromPayload(RequestMessage payload) {
        return new TripRequest() {
            @Override
            public UUID getId() {
                return payload.getId();
            }

            @Override
            public String getHumanReadableId() {
                return payload.getHumanReadableId();
            }

            @Override
            public TransportType getTransportType() {
                return TransportType.valueOf(payload.getTransportType());
            }

            @Override
            public String getTariff() {
                return payload.getTripClass();
            }

            @Override
            public UUID getPassengerId() {
                return payload.getPassengerId();
            }

            @Override
            public UUID getApproverId() {
                return payload.getApprovalId();
            }

            @Override
            public OffsetDateTime getDesiredDate() {
                return payload.getDesiredDate().atOffset(ZoneOffset.UTC);
            }

            @Override
            public OffsetDateTime getApprovalDate() {
                return payload.getApprovalDate() != null ?
                        payload.getApprovalDate().atOffset(ZoneOffset.UTC)
                        : null;
            }

            @Override
            public BigDecimal getPlannedCost() {
                return Optional.ofNullable(payload.getExpected())
                        .map(RequestMessage.ExpectedData::cost)
                        .filter(kopecks -> kopecks >= 0)
                        .map(kopecks -> BigDecimal.valueOf(kopecks)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                        .orElse(null);
            }

            @Override
            public BigDecimal getActualCost() {
                return Optional.ofNullable(payload.getExpected())
                        .map(RequestMessage.ExpectedData::cost)
                        .filter(kopecks -> kopecks >= 0)
                        .map(kopecks -> BigDecimal.valueOf(kopecks)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                        .orElse(null);
            }

            @Override
            public UUID getOrganizationId() {
                return payload.getOrganizationId();
            }

            @Override
            public UUID getDepartmentId() {
                return Optional.ofNullable(payload.getPassenger())
                        .map(RequestMessage.Employee::getDepartmentId)
                        .orElse(null);
            }

            @Override
            public String getRequestStatus() {
                return payload.getStatus();
            }

            @Override
            public UUID getPurposeId() {
                return payload.getPurposeId();
            }

            @Override
            public String getTimeZone() {
                return payload.getTimeZone();
            }

            @Override
            public List<Waypoint> getWaypoints() {
                return payload.getWaypoints().stream()
                        .map(waypoint -> createWaypointFromPayload(waypoint, payload.getId()))
                        .toList();
            }

            @Override
            public Double getDistance() {
                return Optional.ofNullable(payload.getExpected())
                        .map(RequestMessage.ExpectedData::distance)
                        .orElse(null);
            }

            @Override
            public String getCompensationType() {
                if (!CollectionUtils.isEmpty(payload.getTransportCompensation())) {
                    return payload.getTransportCompensation().get(0).compensationType();
                }
                return null;
            }

            @Override
            public Long getDuration() {
                return Optional.ofNullable(payload.getExpected())
                        .map(RequestMessage.ExpectedData::time)
                        .map(Duration::toSeconds)
                        .orElse(null);
            }
        };
    }

    private ru.sber.transport.fraud.monitoring.model.Waypoint createWaypointFromPayload(RequestMessage.Waypoint waypoint, UUID tripRequestId) {
        Objects.requireNonNull(waypoint, "Waypoint cannot be null");
        Objects.requireNonNull(tripRequestId, "TripRequestId cannot be null");

        return new Waypoint() {
            @Override
            public UUID getId() {
                return Optional.ofNullable(waypoint.id())
                        .orElse(UUID.randomUUID());
            }

            @Override
            public UUID getTripRequestId() {
                return tripRequestId;
            }

            @Override
            public String getCountry() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getCountry)
                        .orElse("");
            }

            @Override
            public String getRegion() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getRegion)
                        .orElse("");
            }

            @Override
            public String getCity() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getCity)
                        .orElse("");
            }

            @Override
            public String getStreet() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getStreet)
                        .orElse("");
            }

            @Override
            public String getHouse() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getHouse)
                        .orElse("");
            }

            @Override
            public String getStructure() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getStructure)
                        .orElse("");
            }

            @Override
            public String getBuilding() {
                return Optional.ofNullable(waypoint.address())
                        .map(AddressMessage::getBuilding)
                        .orElse("");
            }

            @Override
            public Integer getOrderingIndex() {
                return Optional.ofNullable(waypoint.orderingIndex())
                        .orElse(0);
            }

            @Override
            public Long getWaitTime() {
                return Optional.ofNullable(waypoint.waitTime())
                        .map(Duration::toMillis)
                        .orElse(0L);
            }

            @Override
            public String getAddress() {
                return Stream.of(
                                waypoint.address().getRegion(),
                                waypoint.address().getCity() != null
                                        && !waypoint.address().getCity().equals(waypoint.address().getRegion())
                                        ? waypoint.address().getCity() : null,
                                waypoint.address().getStreet(),
                                waypoint.address().getHouse()
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "));
            }
        };
    }
}
