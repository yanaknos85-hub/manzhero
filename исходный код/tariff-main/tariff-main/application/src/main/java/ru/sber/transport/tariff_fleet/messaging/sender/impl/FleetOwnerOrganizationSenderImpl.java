package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.FleetOwnerOrganizationSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FleetOwnerOrganizationMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FleetOwnerOrganizationSenderImpl implements FleetOwnerOrganizationSender {
    
    @Qualifier("fleetOwnerOrganizationsOutput")
    private final ObjectProvider<OutputBridge> fleetOwnerOrganizationsOutput;
    @Qualifier("fleetOwnerOrganizationsOutputSsl")
    private final ObjectProvider<OutputBridge> fleetOwnerOrganizationsOutputSsl;
    @Override
    public void send(FleetOwnerOrganizationMessage message) {
        Optional.ofNullable(fleetOwnerOrganizationsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(fleetOwnerOrganizationsOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}