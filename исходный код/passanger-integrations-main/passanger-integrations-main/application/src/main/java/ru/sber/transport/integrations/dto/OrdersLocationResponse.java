package ru.sber.transport.integrations.dto;

import java.util.List;

public record OrdersLocationResponse(
        List<OrderLocation> orderLocations
) {
    
    public record OrderLocation(
            String orderPartnerId,
            OrderCoordinates orderLocation,
            int duration
    ) {
    }
    
    public record OrderCoordinates(
            double latitude,
            double longitude
    ) {
    }
}
