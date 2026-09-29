package ru.sber.transport.integrations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.integrations.constant.OrderClass;
import ru.sber.transport.integrations.dto.*;
import ru.sber.transport.request.messaging.Destination;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sber.transport.request.messaging.Source;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

@Mapper
public interface OrderRequestMapper {

    default String convertLocalDateTimeToString(LocalDateTime ldt, String strTimeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(ldt, TimeZone.getTimeZone("UTC").toZoneId());
        TimeZone timeZone = TimeZone.getTimeZone(strTimeZone);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime.withZoneSameInstant(timeZone.toZoneId()).withNano(0));
    }

    @Mapping(target = "tariff", ignore = true)
    @Mapping(source = "integrationParams.tin", target = "inn")
    @Mapping(source = "status", target = "statusCode")
    @Mapping(source = "workGroup", target = "workGroup")
    @Mapping(target = "planStartTime", expression = "java(convertLocalDateTimeToString(" +
            "taxiTripMessage.information().planStart(), " +
            "taxiTripMessage.timezone()))")
    @Mapping(source = "information.countPassengers", target = "passengerCount")
    @Mapping(source = "tripId", target = "requestId")
    @Mapping(source = "humanId", target = "humanReadableId")
    @Mapping(target = "routePoints",
            expression = "java(createOrderRoutePoints(taxiTripMessage.source(), taxiTripMessage.destination(), taxiTripMessage.waypoints()))")
    @Mapping(source = "commentForDriver", target = "comment")
    @Mapping(source = "transferInformation", target = "information")
    @Mapping(source = "time", target = "expected.time")
    @Mapping(source = "distance", target = "expected.distance")
    @Mapping(source = "transportId", target = "expected.vehicleId")
    @Mapping(target = "propertyClass",
            expression = "java(setPropertyClass(taxiTripMessage.transportType(), taxiTripMessage.taxiClass()))")
    OrderRequest toOrderRequestDTO(OutContractorTaxiTripMessage taxiTripMessage);

    @Mapping(target = "uri", expression = "java(createClientUri(taxiTripMessage.integrationParams().getContractorUrl()))")
    @Mapping(source = "integrationParams.contractorLogin", target = "login")
    @Mapping(source = "integrationParams.contractorPassword", target = "password")
    CredentialClient toCredentialClient(OutContractorTaxiTripMessage taxiTripMessage);

    default OrderRoutePoints createOrderRoutePoints(Source source, Destination destination, List<ru.sber.transport.request.messaging.Waypoint> waypoints) {
        return new OrderRoutePoints(
                new ru.sber.transport.integrations.dto.Source(source.name(), source.latitude(), source.longitude()),
                new ru.sber.transport.integrations.dto.Destination(destination.name(),
                        destination.latitude(),
                        destination.longitude(),
                        contactToContact(destination.contact())),
                waypoints.stream()
                        .map(this::waypointToWaypoint)
                        .toList()
        );
    }

    default Waypoint waypointToWaypoint(ru.sber.transport.request.messaging.Waypoint waypoint) {
        return new Waypoint(waypoint.name(),
                waypoint.latitude(),
                waypoint.longitude(),
                waypoint.waitTime(),
                waypoint.passengers().stream()
                        .map(this::contactToContact)
                        .toList());
    }

    default Contact contactToContact(ru.sber.transport.request.messaging.Contact contact) {
        return new Contact(contact.phone(),
                contact.phone(),
                contact.name(),
                contact.firstName(),
                contact.patronymic(),
                contact.type());
    }

    default URI createClientUri(String baseUrl) {
        return URI.create(baseUrl);
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.of(date, ZoneOffset.UTC);
    }

    /**
     * В рамках проекта Манжерок передаём тип транспорта вместо класса трансфера
     *
     * @param transportTypeEnum {@link TransportTypeEnum}
     * @param taxiClass         класс такси
     * @return propertyClass
     */
    default String setPropertyClass(TransportTypeEnum transportTypeEnum, TaxiClass taxiClass) {
        if (TransportTypeEnum.GROUP_TRANSFER.equals(transportTypeEnum)) {
            return OrderClass.GROUP_TRANSFER.getValue();
        } else {
            return taxiClass.name();
        }
    }
}