package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class EwbContractSenderImpl implements EwbContractSender {
    
    @Qualifier("ewbContractsOutput")
    private final ObjectProvider<OutputBridge> ewbContractsOutput;
    @Qualifier("ewbContractsOutputSsl")
    private final ObjectProvider<OutputBridge> ewbContractsOutputSsl;
    
    @Override
    public void send(EwbContractMessage message) {
        Optional.ofNullable(ewbContractsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(ewbContractsOutputSsl.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
