package ru.sber.transport.integrations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sber.transport.integrations.dto.DriverResponse;
import ru.sber.transport.integrations.dto.OrderInfoResponseExtra;
import ru.sber.transport.integrations.dto.VehicleResponse;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.utils.StringToLocalDateTimeConverter;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage.Driver;
import static ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage.Vehicle;

@Mapper
public interface InContractorTaxiTripInProgressMessageMapper {

    @Mapping(target = "bearing", ignore = true)
    @Mapping(target = "collectionTime", source = "order.collectionTime", qualifiedByName = "mapIsoOffsetDateTime")
    @Mapping(target = "comment", source = "order.comment")
    @Mapping(target = "contractorEmail", ignore = true)
    @Mapping(target = "createOrderTime", source = "order.createOrderTime", qualifiedByName = "mapIsoOffsetDateTime")
    @Mapping(target = "distance", source = "order.distance")
    @Mapping(target = "eta", source = "order.eta")
    @Mapping(target = "finishTime", source = "order.finishTime", qualifiedByName = "mapIsoOffsetDateTime")
    @Mapping(target = "geoLocation", ignore = true)
    @Mapping(target = "geoTime", ignore = true)
    @Mapping(target = "humanId", source = "humanId")
    @Mapping(target = "integrationType", expression = "java(ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType.JSON_API_1_0)")
    @Mapping(target = "isTest", source = "order.isTest")
    @Mapping(target = "performerArrivalTime", source = "order.performerArrivalTime", qualifiedByName = "mapIsoOffsetDateTime")
    @Mapping(target = "price", source = "order.price")
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "status", source = "order.statusCode", qualifiedByName = "mapStatus")
    @Mapping(target = "taxiId", ignore = true)
    @Mapping(target = "transportType", source = "transportType")
    @Mapping(target = "tripId", source = "tripId")
    @Mapping(target = "vehicle", source = "order.driver.vehicle")
    @Mapping(target = "waitTime", source = "order.waitTime")
    @Mapping(target = "waitTimeOW", source = "order.waitTimeOW")
    InContractorTaxiTripInProgressMessage toInProgressMessage(
            OrderInfoResponseExtra order,
            String tripId,
            String humanId,
            TransportTypeEnum transportType
                                                             );
    @Mapping(target = "bearing", ignore = true)
    @Mapping(target = "collectionTime", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "contractorEmail", ignore = true)
    @Mapping(target = "createOrderTime", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "eta", ignore = true)
    @Mapping(target = "finishTime", ignore = true)
    @Mapping(target = "geoLocation", ignore = true)
    @Mapping(target = "geoTime", ignore = true)
    @Mapping(target = "isTest", ignore = true)
    @Mapping(target = "performerArrivalTime", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "resolution", ignore = true)
    @Mapping(target = "status", expression = "java(ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)")
    @Mapping(target = "taxiId", source = "orderPartnerId")
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "waitTime", ignore = true)
    @Mapping(target = "waitTimeOW", ignore = true)
    @Mapping(target = "transportType", defaultValue = "TAXI")
    @Mapping(target = "integrationType", defaultValue = "JSON_API_1_0")
    InContractorTaxiTripInProgressMessage outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(
            OutContractorTaxiTripMessage outContractorTaxiTripMessage,
            String orderPartnerId
                                                                                                             );
    
    Driver driverResponseToDriver(DriverResponse source);

    Vehicle vehicleResponseToVehicle(VehicleResponse source);
    
    @Named("mapStatus")
    default String status(Integer code) {
        return Optional.ofNullable(code)
                       .map(InboundTaxiTripStatus::fromCode)
                       .map(Enum::name)
                       .orElse(null);
    }

    @Named("mapIsoOffsetDateTime")
    default LocalDateTime convertIsoOffsetDateTimeToLocalDateTimeUTC(String dateTimeString) {
        return StringToLocalDateTimeConverter.convert(dateTimeString);
    }
}
