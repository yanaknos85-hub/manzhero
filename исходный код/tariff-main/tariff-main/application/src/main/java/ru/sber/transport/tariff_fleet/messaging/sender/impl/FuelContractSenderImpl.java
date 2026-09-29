package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelContractMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FuelContractSenderImpl implements FuelContractSender {

    @Qualifier("fuelContractsOutput")
    private final ObjectProvider<OutputBridge> fuelContractsOutput;
    @Qualifier("fuelContractsOutputSsl")
    private final ObjectProvider<OutputBridge> fuelContractsOutputSsl;

    @Override
    public void send(FuelContractMessage message) {
        Optional.ofNullable(fuelContractsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(fuelContractsOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
