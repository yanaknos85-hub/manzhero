package ru.sber.transport.integrations.messaging;

import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

public record OrdersLocationMessage(
        UUID id,
        List<OrderLocationMessage> orderLocations
) implements Message<UUID> {
    
    @Override
    public UUID getId() {
        return id;
    }
    
    public record OrderLocationMessage(
            String orderPartnerId,
            OrderCoordinatesMessage orderLocation,
            int duration
    ) {}
    
    public record OrderCoordinatesMessage(
            double latitude,
            double longitude
    ) {}
}
