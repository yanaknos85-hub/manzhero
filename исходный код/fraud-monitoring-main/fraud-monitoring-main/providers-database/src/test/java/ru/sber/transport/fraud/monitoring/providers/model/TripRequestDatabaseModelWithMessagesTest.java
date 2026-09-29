package ru.sber.transport.fraud.monitoring.providers.model;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.fraud_monitoring.tables.records.*;
import ru.sber.transport.fraud.monitoring.model.TripRequestDataWithMessages;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudModelWithMessages;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.MessagingModel;
import ru.sber.transport.fraud.monitoring.providers.trip_request.model.TripRequestDatabaseModelWithMessages;
import ru.sber.transport.fraud.monitoring.providers.waypoint.model.WaypointDatabaseModel;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка модели заявки на поездку с сообщениями")
class TripRequestDatabaseModelWithMessagesTest {

    @Test
    @DisplayName("getFrauds возвращает пустой список, если frauds = null")
    void test_getFrauds_whenNull_returnsEmptyList() {
        final var requestRecord = new TripRequestRecord();
        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        assertThat(model.getFrauds()).isEmpty();
    }

    @Test
    @DisplayName("getFraudMessages возвращает пустую карту, если fraudMessages = null")
    void test_getFraudMessages_whenNull_returnsEmptyMap() {
        final var requestRecord = new TripRequestRecord();
        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        assertThat(model.getFraudMessages()).isEmpty();
    }

    @Test
    @DisplayName("getWaypoints возвращает пустой список, если waypoints = null")
    void test_getWaypoints_whenNull_returnsEmptyList() {
        final var requestRecord = new TripRequestRecord();
        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        assertThat(model.getWaypoints()).isEmpty();
    }

    @Test
    @DisplayName("getFrauds возвращает список нарушений с реальными данными")
    void test_getFrauds_withRealData() {
        final var requestRecord = new TripRequestRecord();
        requestRecord.setId(UUID.randomUUID());

        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(UUID.randomUUID());
        fraudRecord.setRequestId(requestRecord.getId());
        fraudRecord.setAiVerdict("UNKNOWN");
        fraudRecord.setNeedValidation(true);

        final var fraud = new FraudModelWithMessages(fraudRecord);
        model.setFrauds(List.of(fraud));

        final var result = model.getFrauds();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMessaging()).isEmpty();
        assertThat(result.getFirst().getAiVerdict()).isEqualTo("UNKNOWN");
        assertThat(result.getFirst().isNeedValidation()).isTrue();
    }

    @Test
    @DisplayName("getFraudMessages возвращает карту сообщений с реальными данными")
    void test_getFraudMessages_withRealData() {
        final var requestRecord = new TripRequestRecord();
        requestRecord.setId(UUID.randomUUID());

        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        final var fraudId = UUID.randomUUID();
        final var fraudRecord = new FraudRecord();
        fraudRecord.setId(fraudId);
        fraudRecord.setRequestId(requestRecord.getId());

        final var fraud = new FraudModelWithMessages(fraudRecord);
        model.setFrauds(List.of(fraud));

        final var messagingRecord = new MessagingRecord();
        messagingRecord.setId(UUID.randomUUID());
        messagingRecord.setFraudCaseId(fraudId);
        messagingRecord.setBody("Test message body");

        final var msg = new MessagingModel(messagingRecord);
        model.setFraudMessages(Map.of(fraudId, List.of(msg)));

        final var result = model.getFraudMessages();

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(fraudId);
        assertThat(result.get(fraudId)).hasSize(1);
        assertThat(result.get(fraudId).getFirst().getBody()).isEqualTo("Test message body");
    }

    @Test
    @DisplayName("getWaypoints возвращает список путевых точек с реальными данными")
    void test_getWaypoints_withRealData() {
        final var requestRecord = new TripRequestRecord();
        requestRecord.setId(UUID.randomUUID());

        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        final var waypointRecord = new WaypointRecord();
        waypointRecord.setId(UUID.randomUUID());
        waypointRecord.setTripRequestId(requestRecord.getId());
        waypointRecord.setOrderingIndex(0);
        waypointRecord.setCountry("Russia");
        waypointRecord.setRegion("Moscow");

        final var waypoint = new WaypointDatabaseModel(waypointRecord);
        model.setWaypoints(List.of(waypoint));

        final var result = model.getWaypoints();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTripRequestId()).isEqualTo(requestRecord.getId());
    }

    @Test
    @DisplayName("getWaypoints возвращает копию списка для защиты от внешней мутации")
    void test_getWaypoints_returnsDefensiveCopy() {
        final var requestRecord = new TripRequestRecord();
        final var model = new TripRequestDatabaseModelWithMessages(requestRecord);

        final var waypointRecord = new WaypointRecord();
        waypointRecord.setId(UUID.randomUUID());
        waypointRecord.setTripRequestId(requestRecord.getId());
        waypointRecord.setOrderingIndex(0);
        waypointRecord.setCountry("Russia");

        final var waypoint = new WaypointDatabaseModel(waypointRecord);
        model.setWaypoints(List.of(waypoint));

        final var result = model.getWaypoints();

        // Очищаем исходный список — копия не должна измениться
        ((List<TripRequestDataWithMessages>) (List) model.getWaypoints()).clear();

        assertThat(result).hasSize(1);
    }
}