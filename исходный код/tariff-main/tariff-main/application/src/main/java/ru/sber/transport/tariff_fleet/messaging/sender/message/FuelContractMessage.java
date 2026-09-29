package ru.sber.transport.tariff_fleet.messaging.sender.message;

import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FuelContractMessage(
        UUID contractId,
        UUID organizationId,
        UUID contractorId,
        String number,
        LocalDate start,
        LocalDate end,
        String servicePointsName,
        UUID logoS3Id,
        boolean active,
        List<ServicePointMessage> servicePoints
) implements Message<UUID> {

    @Override
    public UUID getId() {
        return contractId;
    }
    
    public record ServicePointMessage(
            UUID id,
            String address,
            double latitude,
            double longitude,
            boolean active
    ) implements Message<UUID> {
        
        @Override
        public UUID getId() {
            return id;
        }
    }
}
