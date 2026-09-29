package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;

public interface EwbTariffSender {
    
    void send(EwbTariffMessage message);
}
