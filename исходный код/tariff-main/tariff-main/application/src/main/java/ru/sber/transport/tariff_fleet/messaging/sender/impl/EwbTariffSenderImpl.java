package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class EwbTariffSenderImpl implements EwbTariffSender {
    
    @Qualifier("ewbTariffsOutput")
    private final ObjectProvider<OutputBridge> ewbTariffsOutput;
    @Qualifier("ewbTariffsOutputSsl")
    private final ObjectProvider<OutputBridge> ewbTariffsOutputSsl;
    
    @Override
    public void send(EwbTariffMessage message) {
        Optional.ofNullable(ewbTariffsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(ewbTariffsOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
