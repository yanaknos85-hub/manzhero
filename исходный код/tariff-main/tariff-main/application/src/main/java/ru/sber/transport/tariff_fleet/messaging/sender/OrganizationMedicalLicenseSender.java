package ru.sber.transport.tariff_fleet.messaging.sender;

import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;

public interface OrganizationMedicalLicenseSender {
    
    void send(OrganizationMedicalLicenseMessage message);
}
