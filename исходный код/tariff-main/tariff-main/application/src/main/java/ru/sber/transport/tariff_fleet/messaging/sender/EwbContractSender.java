package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;

public interface EwbContractSender {
    
    void send(EwbContractMessage message);
}
