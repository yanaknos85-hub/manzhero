package ru.sber.transport.tariff_fleet.messaging.sender;


import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelTariffMessage;

public interface FuelTariffSender {
    
    void send(FuelTariffMessage message);
}
