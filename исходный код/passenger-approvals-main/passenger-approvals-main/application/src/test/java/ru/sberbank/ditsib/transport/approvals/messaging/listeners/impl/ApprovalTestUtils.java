package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import org.instancio.Instancio;
import ru.sberbank.ditsib.transport.approvals.messaging.message.AddressMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;

/**
 * Utils test class
 */
public class ApprovalTestUtils {
    public static RequestMessage baseRequestWithSharedRideMsg() {
        RequestMessage result = baseRequestMsg();
        result.setRideId(UUID.randomUUID());
        result.setSharedRideOwner(true);
        result.setCoopTrip(true);
        result.setTariffId(UUID.randomUUID());
        return result;
    }

    public static RequestMessage baseRequestMsg() {
        return baseRequestMsg(UUID.randomUUID());
    }

    public static RequestMessage baseRequestMsg(UUID tariffId) {
        var address = AddressMessage.builder().building("Building")
                .city("City")
                .country("Country")
                .house("House")
                .latitude(10.11)
                .longitude(12.13)
                .region("Region")
                .street("Street")
                .structure("Structure")
                .build();
        var waypoints = List.of(RequestMessage.Waypoint.builder()
                .address(address)
                .waitTime(Duration.ZERO.plusDays(9))
                .build());
        var expected = RequestMessage.ExpectedData.builder().cost(4.5)
                .distance(6.7).time(Duration.ZERO.plusDays(8)).build();
        return Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getId), UUID.randomUUID())
                .set(field(RequestMessage::getApprovalId), UUID.randomUUID())
                .set(field(RequestMessage::getCommentForDriver), "Comment")
                .set(field(RequestMessage::isCoopTrip), false)
                .set(field(RequestMessage::getCreationTime), LocalDateTime.now().plusDays(1))
                .set(field(RequestMessage::getDesiredDate), LocalDateTime.now().plusDays(2))
                .set(field(RequestMessage::getExpected), expected)
                .set(field(RequestMessage::getHumanReadableId), "HRI")
                .set(field(RequestMessage::getPassengerCount), 4)
                .set(field(RequestMessage::getPassengerId), UUID.randomUUID())
                .set(field(RequestMessage::getStatus), TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .set(field(RequestMessage::getTariffId), tariffId)
                .set(field(RequestMessage::getTransportType), "TAXI")
                .set(field(RequestMessage::getTransportCompensation), List.of(RequestMessage.TransportCompensation.builder()
                        .compensationType(PublicCompensationType.TRAVEL_CARD_COMPENSATION.name())
                        .transportType(TransportTypeEnum.PUBLIC.name())
                        .ticketsCost(40)
                        .build()))
                .set(field(RequestMessage::getTripClass), "BUSINESS")
                .set(field(RequestMessage::getPurposeId), UUID.randomUUID())
                .set(field(RequestMessage::getWaypoints), waypoints)
                .set(field(RequestMessage::isDeleted), false)
                .create();
    }

    public static RequestMessage createCustomRequestMessage(
            double cost, UUID tariffId, String transportType, UUID purposeId) {
        var address = AddressMessage.builder().building("Building")
                .city("City")
                .country("Country")
                .house("House")
                .latitude(10.11)
                .longitude(12.13)
                .region("Region")
                .street("Street")
                .structure("Structure")
                .build();

        var waypoint = RequestMessage.Waypoint.builder().address(address)
                .waitTime(Duration.ZERO.plusDays(9)).build();
        var waypoints = List.of(waypoint);
        var expected = RequestMessage.ExpectedData.builder().cost(cost)
                .distance(6.7).time(Duration.ZERO.plusDays(8)).build();

        return Instancio.of(RequestMessage.class)
                .set(field(RequestMessage::getId), UUID.randomUUID())
                .set(field(RequestMessage::getApprovalId), UUID.randomUUID())
                .set(field(RequestMessage::getCommentForDriver), "Comment")
                .set(field(RequestMessage::isCoopTrip), false)
                .set(field(RequestMessage::getCreationTime), LocalDateTime.now().plusDays(1))
                .set(field(RequestMessage::getDesiredDate), LocalDateTime.now().plusDays(2))
                .set(field(RequestMessage::getExpected), expected)
                .set(field(RequestMessage::getHumanReadableId), "HRI")
                .set(field(RequestMessage::getPassengerCount), 4)
                .set(field(RequestMessage::getPassengerId), UUID.randomUUID())
                .set(field(RequestMessage::getStatus), TripRequestStatus.TAXI_AWAITING_APPROVAL.name())
                .set(field(RequestMessage::getTariffId), tariffId)
                .set(field(RequestMessage::getTransportType), transportType)
                .set(field(RequestMessage::getTransportCompensation), List.of(RequestMessage.TransportCompensation.builder()
                        .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION.name())
                        .transportType(TransportTypeEnum.PUBLIC.name())
                        .ticketsCost(40)
                        .build()))
                .set(field(RequestMessage::getTripClass), "BUSINESS")
                .set(field(RequestMessage::getPurposeId), purposeId)
                .set(field(RequestMessage::getWaypoints), waypoints)
                .set(field(RequestMessage::isDeleted), false)
                .create();
    }
}