package ru.sber.transport.integrations.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.integrations.messaging.listeners.message.CarLocationMessage;
import ru.sber.transport.integrations.provider.CarLocationProvider;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import java.util.Queue;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class ListenerConfig {

    @Bean
    Consumer<Message<OutContractorTaxiTripMessage>> contractorTaxiTripInput(
            Queue<Message<OutContractorTaxiTripMessage>> commonApiQueue
    ) {
        return message -> {
            var payload = message.getPayload();
            log.info("Received OutContractorTaxiTripMessage, taxiId:{}, message:{}", message.getPayload().taxiId(), message);
            if (payload.integrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0)) {
                commonApiQueue.add(message);
            }
        };
    }

    @Bean
    Consumer<Message<CarLocationMessage>> carLocationRequestInput(CarLocationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            log.debug("Car location request received: {}", payload);
            provider.getCarLocation(payload.contractorRequests());
        };
    }

}
