package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelContractMessage;

public interface FuelContractSender {
    
    void send(FuelContractMessage message);
}
