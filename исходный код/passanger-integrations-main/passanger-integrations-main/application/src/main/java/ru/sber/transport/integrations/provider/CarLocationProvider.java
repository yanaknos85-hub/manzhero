package ru.sber.transport.integrations.provider;

import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;

import java.util.Map;
import java.util.UUID;

public interface CarLocationProvider {
    
    void getCarLocation(Map<UUID, CarLocationMessage.ContractorInfo> contractorRequests);
}
