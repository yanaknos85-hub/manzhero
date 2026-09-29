package ru.sber.transport.tariff_fleet.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairTariffMessage;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RepairTariffSenderImpl implements RepairTariffSender {
    @Qualifier("repairTariffsOutput")
    private final ObjectProvider<OutputBridge> repairTariffOutput;
    @Qualifier("repairTariffsOutputSsl")
    private final ObjectProvider<OutputBridge> repairTariffOutputSsl;

    @Override
    public void send(RepairTariffMessage message) {
        Optional.ofNullable(repairTariffOutput.getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
        Optional.ofNullable(repairTariffOutputSsl
                .getIfAvailable()).ifPresent(outputBridge -> outputBridge.send(message));
    }
}
