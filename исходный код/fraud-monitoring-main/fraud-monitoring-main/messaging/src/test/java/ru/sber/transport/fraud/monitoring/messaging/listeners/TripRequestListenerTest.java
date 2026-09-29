package ru.sber.transport.fraud.monitoring.messaging.listeners;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.model.TransportType;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.fraud.monitoring.model.Waypoint;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;
import ru.sber.transport.request.messaging.RequestMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;


@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных о заявке на поездку из кафки")
class TripRequestListenerTest {

    private final TripRequestsService tripRequestsService = mock(TripRequestsService.class);

    private final Consumer<Message<RequestMessage>> input = new TripRequestListener(tripRequestsService);

    @Test
    @DisplayName("Получение заявок на поездку")
    void testValidTransportType() {
        final var waypoints = Instancio.ofList(RequestMessage.Waypoint.class)
                .size(2)
                .create();

        final var message = RequestMessage.builder()
                .id(UUID.randomUUID())
                .waypoints(waypoints)
                .passenger(Instancio.create(RequestMessage.Employee.class))
                .transportType("PUBLIC")
                .expected(Instancio.create(RequestMessage.ExpectedData.class))
                .desiredDate(Instancio.create(LocalDateTime.class))
                .approvalDate(Instancio.create(LocalDateTime.class))
                .approvalId(UUID.randomUUID())
                .transportCompensation(List.of(Instancio.create(RequestMessage.TransportCompensation.class)))
                .timeZone("+3:00")
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(TripRequest.class);
        verify(tripRequestsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertTripRequestBasicFields(actual, message);
        assertTripRequestCostFields(actual, message);

        assertThat(actual.getCompensationType()).isEqualTo(message.getTransportCompensation().get(0).compensationType());

        assertThat(actual.getWaypoints()).hasSameSizeAs(waypoints);
        assertWaypoints(actual.getWaypoints(), waypoints, message.getId());
    }

    private void assertTripRequestBasicFields(TripRequest actual, RequestMessage message) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(message.getId());
            softly.assertThat(actual.getDesiredDate()).isNotNull();
            softly.assertThat(actual.getDesiredDate().toLocalDateTime()).isEqualTo(message.getDesiredDate());
            softly.assertThat(actual.getApprovalDate().toLocalDateTime()).isEqualTo(message.getApprovalDate());
            softly.assertThat(actual.getPassengerId()).isEqualTo(message.getPassengerId());
            softly.assertThat(actual.getDepartmentId()).isEqualTo(message.getPassenger().getDepartmentId());
            softly.assertThat(actual.getApproverId()).isEqualTo(message.getApprovalId());
            softly.assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
            softly.assertThat(actual.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
            softly.assertThat(actual.getRequestStatus()).isEqualTo(message.getStatus());
            softly.assertThat(actual.getTransportType()).isEqualTo(TransportType.PUBLIC);
            softly.assertThat(actual.getTimeZone()).isEqualTo(message.getTimeZone());
            softly.assertThat(actual.getPurposeId()).isEqualTo(message.getPurposeId());
            softly.assertThat(actual.getTariff()).isEqualTo(message.getTripClass());
            softly.assertThat(actual.getDistance()).isEqualTo(message.getExpected().distance());
        });
    }

    private void assertTripRequestCostFields(TripRequest actual, RequestMessage message) {
        final var expectedCost = BigDecimal.valueOf(message.getExpected().cost())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertSoftly(softly -> {
            softly.assertThat(actual.getPlannedCost()).isEqualTo(expectedCost);
            softly.assertThat(actual.getActualCost()).isEqualTo(expectedCost);
        });
    }

    private void assertWaypoints(List<Waypoint> actualWaypoints, List<RequestMessage.Waypoint> expectedWaypoints, UUID tripRequestId) {
        for (int i = 0; i < expectedWaypoints.size(); i++) {
            final var expectedWaypoint = expectedWaypoints.get(i);
            final var actualWaypoint = actualWaypoints.get(i);

            assertWaypointBasicFields(actualWaypoint, expectedWaypoint, tripRequestId);
            assertThat(actualWaypoint.getAddress()).isNotBlank();
        }
    }

    private void assertWaypointBasicFields(Waypoint actual, RequestMessage.Waypoint expected, UUID tripRequestId) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(expected.id());
            softly.assertThat(actual.getTripRequestId()).isEqualTo(tripRequestId);
            softly.assertThat(actual.getOrderingIndex()).isEqualTo(expected.orderingIndex());
            softly.assertThat(actual.getCountry()).isEqualTo(expected.address().getCountry());
            softly.assertThat(actual.getRegion()).isEqualTo(expected.address().getRegion());
            softly.assertThat(actual.getCity()).isEqualTo(expected.address().getCity());
            softly.assertThat(actual.getStreet()).isEqualTo(expected.address().getStreet());
            softly.assertThat(actual.getHouse()).isEqualTo(expected.address().getHouse());
            softly.assertThat(actual.getStructure()).isEqualTo(expected.address().getStructure());
            softly.assertThat(actual.getBuilding()).isEqualTo(expected.address().getBuilding());
            if (expected.waitTime() != null) {
                assertThat(actual.getWaitTime()).isEqualTo(expected.waitTime().toMillis());
            } else {
                assertThat(actual.getWaitTime()).isZero();
            }
        });
    }

    @Test
    @DisplayName("Получение заявок на поездку для некорректного типа транспорта")
    void testInvalidTransportType() {
        final var message = RequestMessage.builder()
                .id(UUID.randomUUID())
                .waypoints(Instancio.createList(RequestMessage.Waypoint.class))
                .passenger(Instancio.create(RequestMessage.Employee.class))
                .transportType("UNKNOWN")
                .expected(Instancio.create(RequestMessage.ExpectedData.class))
                .desiredDate(Instancio.create(LocalDateTime.class))
                .approvalDate(Instancio.create(LocalDateTime.class))
                .approvalId(UUID.randomUUID())
                .timeZone("+3:00")
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(TripRequest.class);

        verify(tripRequestsService, never()).createOrUpdate(messageCaptor.capture());
    }
}