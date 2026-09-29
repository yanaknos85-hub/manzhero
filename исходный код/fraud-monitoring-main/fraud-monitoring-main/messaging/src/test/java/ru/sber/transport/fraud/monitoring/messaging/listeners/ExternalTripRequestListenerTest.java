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
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.TripRequestMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.WaypointMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.TripRequestImpl;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.WaypointImpl;
import ru.sber.transport.fraud.monitoring.model.TripRequest;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;


@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных о заявке на поездку из кафки (топик внешних заявок)")
class ExternalTripRequestListenerTest {

    private final TripRequestsService tripRequestsService = mock(TripRequestsService.class);
    private final TripRequestMapper mapper = mock(TripRequestMapper.class);
    private final WaypointMapper waypointMapper = mock(WaypointMapper.class);

    private final Consumer<Message<ExternalRequestMessage>> input = new ExternalTripRequestListener(tripRequestsService,
            mapper, waypointMapper);

    @Test
    @DisplayName("Получение заявок на поездку (Яндекс Go)")
    void test() {
        final var waypoint1 = ExternalRequestMessage.WaypointMessage.builder()
                .id(UUID.randomUUID())
                .country(Instancio.create(String.class))
                .region(Instancio.create(String.class))
                .city(Instancio.create(String.class))
                .street(Instancio.create(String.class))
                .house(Instancio.create(String.class))
                .structure(Instancio.create(String.class))
                .building(Instancio.create(String.class))
                .longitude(Instancio.create(Double.class))
                .latitude(Instancio.create(Double.class))
                .build();

        final var message = Instancio.of(ExternalRequestMessage.class)
                .set(field(ExternalRequestMessage::getWaypoints), List.of(waypoint1))
                .create();

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