package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.model.WaypointImpl;
import ru.sber.transport.messages.request.external.avro.WaypointMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WaypointMapperTest {

    private final WaypointMapper waypointMapper = new WaypointMapperImpl();

    @Test
    void waypointMessageToWaypointImpl() {
        final var expectedWaypoint = ExternalRequestMessage.WaypointMessage.builder()
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
        final var expectedWaypoint2 = WaypointMessage.newBuilder()
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
        final var tripRequestId = UUID.randomUUID();

        final var actualWaypoint = waypointMapper.waypointMessageToWaypointImpl(expectedWaypoint, 0, tripRequestId);
        final var actualWaypoint2 = waypointMapper.waypointMessageToWaypointImpl(expectedWaypoint2, 0, tripRequestId);

        assertWaypoint(actualWaypoint, expectedWaypoint);
        assertWaypoint(actualWaypoint2, expectedWaypoint2);
        assertThat(actualWaypoint.getTripRequestId()).isEqualTo(tripRequestId);
        assertThat(actualWaypoint2.getTripRequestId()).isEqualTo(tripRequestId);
    }

    private void assertWaypoint(WaypointImpl actualWaypoint, WaypointMessage expected) {
        assertThat(actualWaypoint).isNotNull();
        assertThat(actualWaypoint.getId()).isEqualTo(expected.getId());
        assertThat(actualWaypoint.getOrderingIndex()).isEqualTo(0);
        assertThat(actualWaypoint.getCountry()).isEqualTo(expected.getCountry());
        assertThat(actualWaypoint.getRegion()).isEqualTo(expected.getRegion());
        assertThat(actualWaypoint.getCity()).isEqualTo(expected.getCity());
        assertThat(actualWaypoint.getStreet()).isEqualTo(expected.getStreet());
        assertThat(actualWaypoint.getHouse()).isEqualTo(expected.getHouse());
        assertThat(actualWaypoint.getStructure()).isEqualTo(expected.getStructure());
        assertThat(actualWaypoint.getBuilding()).isEqualTo(expected.getBuilding());
        assertThat(actualWaypoint.getWaitTime()).isZero();
        assertThat(actualWaypoint.getAddress()).isNotBlank();
    }

    private void assertWaypoint(WaypointImpl actualWaypoint, ExternalRequestMessage.WaypointMessage expected) {
        assertThat(actualWaypoint).isNotNull();
        assertThat(actualWaypoint.getId()).isEqualTo(expected.getId());
        assertThat(actualWaypoint.getOrderingIndex()).isEqualTo(0);
        assertThat(actualWaypoint.getCountry()).isEqualTo(expected.getCountry());
        assertThat(actualWaypoint.getRegion()).isEqualTo(expected.getRegion());
        assertThat(actualWaypoint.getCity()).isEqualTo(expected.getCity());
        assertThat(actualWaypoint.getStreet()).isEqualTo(expected.getStreet());
        assertThat(actualWaypoint.getHouse()).isEqualTo(expected.getHouse());
        assertThat(actualWaypoint.getStructure()).isEqualTo(expected.getStructure());
        assertThat(actualWaypoint.getBuilding()).isEqualTo(expected.getBuilding());
        assertThat(actualWaypoint.getWaitTime()).isZero();
        assertThat(actualWaypoint.getAddress()).isNotBlank();
    }
}
