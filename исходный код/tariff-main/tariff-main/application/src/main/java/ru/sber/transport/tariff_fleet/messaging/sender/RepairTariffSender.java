package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairTariffMessage;

public interface RepairTariffSender {
    void send(RepairTariffMessage message);
}
