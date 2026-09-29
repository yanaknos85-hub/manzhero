package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairContractMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RepairContractSenderImpl implements RepairContractSender {

    @Qualifier("repairContractsOutput")
    private final ObjectProvider<OutputBridge> repairContractsOutput;
    @Qualifier("repairContractsOutputSsl")
    private final ObjectProvider<OutputBridge> repairContractsOutputSsl;

    @Override
    public void send(RepairContractMessage message) {
        Optional.ofNullable(repairContractsOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(repairContractsOutputSsl
                .getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
