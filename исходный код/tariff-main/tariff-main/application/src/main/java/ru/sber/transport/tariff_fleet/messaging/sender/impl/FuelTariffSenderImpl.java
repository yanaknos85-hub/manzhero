package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelTariffMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FuelTariffSenderImpl implements FuelTariffSender {
    
    @Qualifier("fuelTariffsOutput")
    private final ObjectProvider<OutputBridge> fuelTariffsOutput;
    @Qualifier("fuelTariffsOutputSsl")
    private final ObjectProvider<OutputBridge> fuelTariffsOutputSsl;
    
    @Override
    public void send(FuelTariffMessage message) {
        Optional.ofNullable(fuelTariffsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(fuelTariffsOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
