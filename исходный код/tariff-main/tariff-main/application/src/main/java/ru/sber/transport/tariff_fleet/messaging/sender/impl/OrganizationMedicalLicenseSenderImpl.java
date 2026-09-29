package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.OrganizationMedicalLicenseSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrganizationMedicalLicenseSenderImpl implements OrganizationMedicalLicenseSender {
    
    @Qualifier("organizationMedicalLicensesOutput")
    private final ObjectProvider<OutputBridge> organizationMedicalLicensesOutput;
    @Qualifier("organizationMedicalLicensesOutputSsl")
    private final ObjectProvider<OutputBridge> organizationMedicalLicensesOutputSsl;
    
    @Override
    public void send(OrganizationMedicalLicenseMessage message) {
        Optional.ofNullable(organizationMedicalLicensesOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(organizationMedicalLicensesOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
