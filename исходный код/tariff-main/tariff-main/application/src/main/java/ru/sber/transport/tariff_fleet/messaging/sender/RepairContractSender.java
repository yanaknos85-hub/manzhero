package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairContractMessage;

public interface RepairContractSender {
    
    void send(RepairContractMessage message);
}
