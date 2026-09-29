package ru.sber.transport.integrations.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.messaging.sender.InContractorTaxiTripInProgressSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.HashMap;


/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class InContractorTaxiTripInProgressSenderImpl implements InContractorTaxiTripInProgressSender {
    
    @Qualifier("inContractorTaxiTripInProgressOutput")
    private final ObjectProvider<OutputBridge> inContractorTaxiTripInProgressOutput;
    
    @Override
    public void send(InContractorTaxiTripInProgressMessage message) {
        log.info("Send message, taxiId:{}, tripId:{}, message:{}", message.taxiId(), message.tripId(), message);
        var headers = new HashMap<String, Object>();
        headers.put(KafkaHeaders.KEY, message.getId());
        inContractorTaxiTripInProgressOutput.ifAvailable(it -> it.send(message, headers));
        log.info("Sent message, taxiId:{}, tripId:{}", message.taxiId(), message.tripId());
    }
}