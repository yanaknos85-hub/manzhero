package ru.sber.transport.integrations.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.integrations.dto.DriverResponse;
import ru.sber.transport.integrations.dto.OrderInfoResponseExtra;
import ru.sber.transport.integrations.dto.VehicleResponse;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class InContractorTaxiTripInProgressMessageMapperTest {

    private final InContractorTaxiTripInProgressMessageMapper mapper = Mappers.getMapper(InContractorTaxiTripInProgressMessageMapper.class);

    @Test
    void toInProgressMessage() {
        var someDate = "2011-12-03T10:15:30+01:00";
        var order = Instancio.of(OrderInfoResponseExtra.class)
                .set(field(OrderInfoResponseExtra::getPerformerArrivalTime), someDate)
                .set(field(OrderInfoResponseExtra::getCreateOrderTime), someDate)
                .set(field(OrderInfoResponseExtra::getCollectionTime), someDate)
                .set(field(OrderInfoResponseExtra::getFinishTime), someDate)
                .set(field(OrderInfoResponseExtra::getStatusCode), 7)
                .create();
        var tripId = Instancio.create(String.class);
        var humanId = Instancio.create(String.class);
        var transportType = Instancio.create(TransportTypeEnum.class);
        assertThat(mapper.toInProgressMessage(order, tripId, humanId, transportType))
                .isNotNull()
                .extracting(
                        InContractorTaxiTripInProgressMessage::bearing,
                        InContractorTaxiTripInProgressMessage::collectionTime,
                        InContractorTaxiTripInProgressMessage::comment,
                        InContractorTaxiTripInProgressMessage::contractorEmail,
                        InContractorTaxiTripInProgressMessage::createOrderTime,
                        InContractorTaxiTripInProgressMessage::distance,
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().companyId(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().id(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().imageUrl(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().name(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().patronymic(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().phone(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().rating(),
                        inContractorTaxiTripInProgressMessage1 -> inContractorTaxiTripInProgressMessage1.driver().secName(),
                        InContractorTaxiTripInProgressMessage::eta,
                        InContractorTaxiTripInProgressMessage::finishTime,
                        InContractorTaxiTripInProgressMessage::geoLocation,
                        InContractorTaxiTripInProgressMessage::geoTime,
                        InContractorTaxiTripInProgressMessage::humanId,
                        InContractorTaxiTripInProgressMessage::integrationType,
                        InContractorTaxiTripInProgressMessage::isTest,
                        InContractorTaxiTripInProgressMessage::performerArrivalTime,
                        InContractorTaxiTripInProgressMessage::price,
                        InContractorTaxiTripInProgressMessage::resolution,
                        InContractorTaxiTripInProgressMessage::status,
                        InContractorTaxiTripInProgressMessage::taxiId,
                        InContractorTaxiTripInProgressMessage::transportType,
                        InContractorTaxiTripInProgressMessage::tripId,
                        inContractorTaxiTripInProgressMessage -> inContractorTaxiTripInProgressMessage.vehicle().color(),
                        inContractorTaxiTripInProgressMessage -> inContractorTaxiTripInProgressMessage.vehicle().mark(),
                        inContractorTaxiTripInProgressMessage -> inContractorTaxiTripInProgressMessage.vehicle().model(),
                        inContractorTaxiTripInProgressMessage -> inContractorTaxiTripInProgressMessage.vehicle().registrationNumber(),
                        InContractorTaxiTripInProgressMessage::waitTime,
                        InContractorTaxiTripInProgressMessage::waitTimeOW
                )
                .containsExactly(
                        null,
                        convertStringToLocalDateTime(order.getCollectionTime()),
                        order.getComment(),
                        null,
                        convertStringToLocalDateTime(order.getCreateOrderTime()),
                        order.getDistance(),
                        order.getDriver().getCompanyId().toString(),
                        order.getDriver().getId().toString(),
                        order.getDriver().getImageUrl(),
                        order.getDriver().getName(),
                        order.getDriver().getPatronymic(),
                        order.getDriver().getPhone(),
                        order.getDriver().getRating().toString(),
                        order.getDriver().getSecName(),
                        order.getEta(),
                        convertStringToLocalDateTime(order.getFinishTime()),
                        null,
                        null,
                        humanId,
                        TaxiExternalIntegrationType.JSON_API_1_0,
                        order.getIsTest(),
                        convertStringToLocalDateTime(order.getPerformerArrivalTime()),
                        order.getPrice(),
                        null,
                        InboundTaxiTripStatus.fromCode(order.getStatusCode()),
                        null,
                        transportType,
                        tripId,
                        order.getDriver().getVehicle().getColor(),
                        order.getDriver().getVehicle().getMark(),
                        order.getDriver().getVehicle().getModel(),
                        order.getDriver().getVehicle().getRegistrationNumber(),
                        order.getWaitTime(),
                        order.getWaitTimeOW()
                );
        assertThat(mapper.toInProgressMessage(null, null, null, null)).isNull();
    }

    @Test
    void outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage() {
        var outContractorTaxiTripMessage = Instancio.create(OutContractorTaxiTripMessage.class);
        var orderPartnerId = Instancio.create(String.class);
        assertThat(mapper.outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(outContractorTaxiTripMessage, orderPartnerId))
                .isNotNull()
                .extracting(
                        InContractorTaxiTripInProgressMessage::bearing,
                        InContractorTaxiTripInProgressMessage::collectionTime,
                        InContractorTaxiTripInProgressMessage::comment,
                        InContractorTaxiTripInProgressMessage::contractorEmail,
                        InContractorTaxiTripInProgressMessage::createOrderTime,
                        InContractorTaxiTripInProgressMessage::distance,
                        InContractorTaxiTripInProgressMessage::driver,
                        InContractorTaxiTripInProgressMessage::eta,
                        InContractorTaxiTripInProgressMessage::finishTime,
                        InContractorTaxiTripInProgressMessage::geoLocation,
                        InContractorTaxiTripInProgressMessage::geoTime,
                        InContractorTaxiTripInProgressMessage::humanId,
                        InContractorTaxiTripInProgressMessage::integrationType,
                        InContractorTaxiTripInProgressMessage::isTest,
                        InContractorTaxiTripInProgressMessage::performerArrivalTime,
                        InContractorTaxiTripInProgressMessage::price,
                        InContractorTaxiTripInProgressMessage::resolution,
                        InContractorTaxiTripInProgressMessage::status,
                        InContractorTaxiTripInProgressMessage::taxiId,
                        InContractorTaxiTripInProgressMessage::transportType,
                        InContractorTaxiTripInProgressMessage::tripId,
                        InContractorTaxiTripInProgressMessage::vehicle,
                        InContractorTaxiTripInProgressMessage::waitTime,
                        InContractorTaxiTripInProgressMessage::waitTimeOW
                )
                .containsExactly(
                        null,
                        null,
                        null,
                        null,
                        null,
                        outContractorTaxiTripMessage.distance(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        outContractorTaxiTripMessage.humanId(),
                        outContractorTaxiTripMessage.integrationType(),
                        null,
                        null,
                        null,
                        null,
                        InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT,
                        orderPartnerId,
                        outContractorTaxiTripMessage.transportType(),
                        outContractorTaxiTripMessage.tripId(),
                        null,
                        null,
                        null
                );
        assertThat(mapper.outContractorTaxiTripMessageToInContractorTaxiTripInProgressMessage(null, null)).isNull();
    }

    @Test
    void driverResponseToDriver() {
        var driverResponse = Instancio.create(DriverResponse.class);
        var actual = mapper.driverResponseToDriver(driverResponse);
        assertThat(actual)
                .isNotNull()
                .extracting(
                        InContractorTaxiTripInProgressMessage.Driver::companyId,
                        InContractorTaxiTripInProgressMessage.Driver::id,
                        InContractorTaxiTripInProgressMessage.Driver::imageUrl,
                        InContractorTaxiTripInProgressMessage.Driver::name,
                        InContractorTaxiTripInProgressMessage.Driver::patronymic,
                        InContractorTaxiTripInProgressMessage.Driver::phone,
                        InContractorTaxiTripInProgressMessage.Driver::rating,
                        InContractorTaxiTripInProgressMessage.Driver::secName
                )
                .containsExactly(
                        driverResponse.getCompanyId().toString(),
                        driverResponse.getId().toString(),
                        driverResponse.getImageUrl(),
                        driverResponse.getName(),
                        driverResponse.getPatronymic(),
                        driverResponse.getPhone(),
                        driverResponse.getRating().toString(),
                        driverResponse.getSecName()
                );
        assertThat(mapper.driverResponseToDriver(null)).isNull();
    }

    @Test
    void vehicleResponseToVehicle() {
        var vehicleResponse = Instancio.create(VehicleResponse.class);
        assertThat(mapper.vehicleResponseToVehicle(vehicleResponse))
                .isNotNull()
                .extracting(
                        InContractorTaxiTripInProgressMessage.Vehicle::color,
                        InContractorTaxiTripInProgressMessage.Vehicle::mark,
                        InContractorTaxiTripInProgressMessage.Vehicle::model,
                        InContractorTaxiTripInProgressMessage.Vehicle::registrationNumber
                )
                .containsExactly(
                        vehicleResponse.getColor(),
                        vehicleResponse.getMark(),
                        vehicleResponse.getModel(),
                        vehicleResponse.getRegistrationNumber()
                );
        assertThat(mapper.vehicleResponseToVehicle(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(InboundTaxiTripStatus.class)
    void status(InboundTaxiTripStatus inboundTaxiTripStatus) {
        assertThat(mapper.status(inboundTaxiTripStatus.getCode())).isEqualTo(inboundTaxiTripStatus.getName());
    }

    @Test
    void status_null() {
        assertThat(mapper.status(777)).isNull();
        assertThat(mapper.status(null)).isNull();
        assertThat(mapper.status(-3)).isNull();
    }

    private LocalDateTime convertStringToLocalDateTime(String date) {
        return ZonedDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .withZoneSameInstant(TimeZone.getTimeZone(ZoneOffset.UTC.getId()).toZoneId())
                .toLocalDateTime();
    }
}