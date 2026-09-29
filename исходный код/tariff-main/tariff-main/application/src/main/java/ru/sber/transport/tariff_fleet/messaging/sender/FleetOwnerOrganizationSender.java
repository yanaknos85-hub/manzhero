package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.FleetOwnerOrganizationMessage;

//TODO кажется, не пригодится. Удалить после 1 июня
public interface FleetOwnerOrganizationSender {
    
    void send(FleetOwnerOrganizationMessage message);
}
