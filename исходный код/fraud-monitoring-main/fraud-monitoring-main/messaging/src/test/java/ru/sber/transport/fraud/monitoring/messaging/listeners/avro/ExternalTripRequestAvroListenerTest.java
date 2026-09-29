package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

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
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.TripRequestMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.WaypointMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.TripRequestImpl;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.WaypointImpl;
import ru.sber.transport.fraud.monitoring.model.TripRequest;
import ru.sber.transport.messages.request.external.avro.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных о заявке на поездку из кафки (топик внешних заявок)")
class ExternalTripRequestAvroListenerTest {

    private final TripRequestsService tripRequestsService = mock(TripRequestsService.class);
    private final TripRequestMapper mapper = mock(TripRequestMapper.class);
    private final WaypointMapper waypointMapper = mock(WaypointMapper.class);
    private final Consumer<Message<RequestMessage>> input = new ExternalTripRequestAvroListener(tripRequestsService,
            mapper, waypointMapper);

    @Test
    @DisplayName("Получение заявок на поездку (Яндекс Go)")
    void test() {
        final var waypoint1 = WaypointMessage.newBuilder()
                .setId(UUID.randomUUID())
                .setCountry(Instancio.create(String.class))
                .setRegion(Instancio.create(String.class))
                .setCity(Instancio.create(String.class))
                .setStreet(Instancio.create(String.class))
                .setHouse(Instancio.create(String.class))
                .setStructure(Instancio.create(String.class))
                .setBuilding(Instancio.create(String.class))
                .setLongitude(Instancio.create(Double.class))
                .setLatitude(Instancio.create(Double.class))
                .build();

        final var message = RequestMessage.newBuilder()
                .setStatus(StatusMessage.CONFIRMATION)
                .setId(UUID.randomUUID())
                .setHumanReadableId(Instancio.create(String.class))
                .setWaypoints(List.of(waypoint1))
                .setPassengerId(UUID.randomUUID())
                .setTariff(Instancio.create(TariffMessage.class))
                .setPurposeId(UUID.randomUUID())
                .setPlanned(Instancio.create(PlannedData.class))
                .setDate(Instant.now())
                .setActual(Instancio.create(ActualData.class))
                .setTimeZone("+3:00")
                .setOrganizationId(UUID.randomUUID())
                .setDepartmentId(UUID.randomUUID())
                .setApproverId(UUID.randomUUID())
                .setApprovalDate(Instant.now())
                .build();

        doReturn(Instancio.create(TripRequestImpl.class)).when(mapper).externalRequestMessageToTripRequest(message);
        doReturn(Instancio.create(WaypointImpl.class)).doReturn(Instancio.create(WaypointImpl.class))
                .when(waypointMapper).waypointMessageToWaypointImpl(waypoint1, 0, message.getId());

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(TripRequest.class);
        verify(tripRequestsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertThat(actual).isNotNull();
        assertThat(actual.getWaypoints()).hasSameSizeAs(message.getWaypoints());
    }
}