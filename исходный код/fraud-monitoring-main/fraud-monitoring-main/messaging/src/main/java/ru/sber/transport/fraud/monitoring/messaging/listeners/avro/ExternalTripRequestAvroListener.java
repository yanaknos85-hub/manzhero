package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.TripRequestMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.WaypointMapper;
import ru.sber.transport.fraud.monitoring.model.Waypoint;
import ru.sber.transport.messages.request.external.avro.RequestMessage;

import java.util.ArrayList;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ExternalTripRequestAvroListener implements Consumer<Message<RequestMessage>> {

    private final TripRequestsService tripRequestsService;
    private final TripRequestMapper mapper;
    private final WaypointMapper waypointMapper;

    @Override
    public void accept(Message<RequestMessage> raw) {
        final var payload = raw.getPayload();

        final var tripRequest = mapper.externalRequestMessageToTripRequest(payload);
        var waypoints = new ArrayList<Waypoint>();
        for (int i = 0; i < payload.getWaypoints().size(); i++) {
            var waypointFromPayload = waypointMapper.waypointMessageToWaypointImpl(payload.getWaypoints().get(i), i, payload.getId());
            waypoints.add(waypointFromPayload);
        }
        tripRequest.setWaypoints(waypoints);
        if (payload.getWaypoints().stream().allMatch(waypoint -> waypoint.getId() != null)) {
            tripRequestsService.createOrUpdate(tripRequest);
        }

        log.debug("External trip request saved successfully: {}", tripRequest.getId());
    }
}
