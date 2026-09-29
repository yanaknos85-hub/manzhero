package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.model.TransportType;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.messages.request.external.avro.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;

@DisplayName("Тест маппера запросов на поездку")
class TripRequestMapperTest {

    private final TripRequestMapper mapper = new TripRequestMapperImpl();

    @Test
    @DisplayName("Тест маппинга из кафка сообщения")
    void externalRequestMessageToTripRequest() {
        final var message = ExternalRequestMessage.builder()
                .status("CONFIRMATION")
                .id(UUID.randomUUID())
                .humanReadableId(Instancio.create(String.class))
                .waypoints(List.of())
                .passengerId(UUID.randomUUID())
                .tariff("ECONOMY")
                .purposeId(UUID.randomUUID())
                .planned(Instancio.of(ExternalRequestMessage.PlannedData.class)
                        .set(field(ExternalRequestMessage.PlannedData::getDuration), Duration.ofHours(1).toString())
                        .create())
                .date(LocalDateTime.now())
                .actual(Instancio.create(ExternalRequestMessage.ActualData.class))
                .timeZone("+3:00")
                .organizationId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .approverId(UUID.randomUUID())
                .approvalDate(LocalDateTime.now())
                .build();
        final var messageAvro = RequestMessage.newBuilder()
                .setStatus(StatusMessage.CONFIRMATION)
                .setId(UUID.randomUUID())
                .setHumanReadableId(Instancio.create(String.class))
                .setWaypoints(List.of())
                .setPassengerId(UUID.randomUUID())
                .setTariff(Instancio.create(TariffMessage.class))
                .setPurposeId(UUID.randomUUID())
                .setPlanned(Instancio.of(PlannedData.class)
                        .set(field(PlannedData::getDuration), Duration.ofHours(1).toString())
                        .create())
                .setDate(Instant.now())
                .setActual(Instancio.create(ActualData.class))
                .setTimeZone("+3:00")
                .setOrganizationId(UUID.randomUUID())
                .setDepartmentId(UUID.randomUUID())
                .setApproverId(UUID.randomUUID())
                .setApprovalDate(Instant.now())
                .build();

        final var actual = mapper.externalRequestMessageToTripRequest(message);
        final var actual2 = mapper.externalRequestMessageToTripRequest(messageAvro);

        assertThat(actual).isNotNull();
        assertThat(actual2).isNotNull();
        assertTripRequestFields(actual, message);
        assertTripRequestFields(actual2, messageAvro);
    }

    private void assertTripRequestFields(TripRequest actual, ExternalRequestMessage expected) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(expected.getId());
            softly.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            softly.assertThat(actual.getRequestStatus()).isEqualTo(expected.getStatus());
            softly.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            softly.assertThat(actual.getTariff()).isEqualTo(expected.getTariff());
            softly.assertThat(actual.getDesiredDate().toLocalDateTime()).isEqualTo(expected.getDate());
            softly.assertThat(actual.getPlannedCost()).isEqualTo(BigDecimal.valueOf(expected.getPlanned().getCost()));
            softly.assertThat(actual.getActualCost()).isEqualTo(BigDecimal.valueOf(expected.getActual().getCost()));
            softly.assertThat(actual.getPassengerId()).isEqualTo(expected.getPassengerId());
            softly.assertThat(actual.getTransportType()).isEqualTo(TransportType.YANDEX);
            softly.assertThat(actual.getTimeZone()).isEqualTo(expected.getTimeZone());
            softly.assertThat(actual.getDepartmentId()).isEqualTo(expected.getDepartmentId());
            softly.assertThat(actual.getApproverId()).isEqualTo(expected.getApproverId());
            softly.assertThat(actual.getApprovalDate().toLocalDateTime()).isEqualTo(expected.getApprovalDate());
            softly.assertThat(actual.getOrganizationId()).isEqualTo(expected.getOrganizationId());
            softly.assertThat(actual.getDistance()).isEqualTo(expected.getPlanned().getDistance() / 1000.0);
            softly.assertThat(actual.getCompensationType()).isNull();
        });
    }

    private void assertTripRequestFields(TripRequest actual, RequestMessage expected) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(expected.getId());
            softly.assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            softly.assertThat(actual.getRequestStatus()).isEqualTo(expected.getStatus().name());
            softly.assertThat(actual.getPurposeId()).isEqualTo(expected.getPurposeId());
            softly.assertThat(actual.getTariff()).isEqualTo(expected.getTariff().name());
            softly.assertThat(actual.getDesiredDate().toInstant()).isEqualTo(expected.getDate());
            softly.assertThat(actual.getPlannedCost()).isEqualTo(BigDecimal.valueOf(expected.getPlanned().getCost()));
            softly.assertThat(actual.getActualCost()).isEqualTo(BigDecimal.valueOf(expected.getActual().getCost()));
            softly.assertThat(actual.getPassengerId()).isEqualTo(expected.getPassengerId());
            softly.assertThat(actual.getTransportType()).isEqualTo(TransportType.YANDEX);
            softly.assertThat(actual.getTimeZone()).isEqualTo(expected.getTimeZone());
            softly.assertThat(actual.getDepartmentId()).isEqualTo(expected.getDepartmentId());
            softly.assertThat(actual.getApproverId()).isEqualTo(expected.getApproverId());
            softly.assertThat(actual.getApprovalDate().toInstant()).isEqualTo(expected.getApprovalDate());
            softly.assertThat(actual.getOrganizationId()).isEqualTo(expected.getOrganizationId());
            softly.assertThat(actual.getDistance()).isEqualTo(expected.getPlanned().getDistance() / 1000.0);
            softly.assertThat(actual.getCompensationType()).isNull();
        });
    }
}
