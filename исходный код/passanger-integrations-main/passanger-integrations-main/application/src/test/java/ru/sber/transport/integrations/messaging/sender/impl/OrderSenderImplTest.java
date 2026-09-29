package ru.sber.transport.integrations.messaging.sender.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.sber.transport.integrations.config.ContractorBlockProperties;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.integrations.messaging.sender.OrderSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.logging.model.LogField;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedPostgres
class OrderSenderImplTest extends KafkaTest {
    
    @Autowired
    private OrderSender orderSender;
    @MockitoSpyBean
    @Qualifier("carLocationResponseOutput")
    private OutputBridge carLocationResponseOutput;
    @MockitoBean
    @SuppressWarnings("unused")
    private ContractorBlockProperties contractorBlockProperties;
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;
    
    @Test
    void send() {
        MDC.clear();
        MDC.put(LogField.TRACE_ID.getKey(), UUID.randomUUID().toString());
        var message = new OrdersLocationMessage(
                UUID.randomUUID(),
                List.of(
                        new OrdersLocationMessage.OrderLocationMessage(
                                "firstOrderPartnerId",
                                new OrdersLocationMessage.OrderCoordinatesMessage(
                                        10.0003,
                                        10.0004
                                ),
                                3600
                        ),
                        new OrdersLocationMessage.OrderLocationMessage(
                                "secondOrderPartnerId",
                                new OrdersLocationMessage.OrderCoordinatesMessage(
                                        10.0005,
                                        10.0006
                                ),
                                1200
                        )
                       )
        );
        
        orderSender.send(message);
        var actual = consumeMessage("service.response.car-location-response", OrdersLocationMessage.class);
        verify(carLocationResponseOutput).send(any(OrdersLocationMessage.class), mapArgumentCaptor.capture());
        assertThat(mapArgumentCaptor.getAllValues())
                .hasSize(1)
                .extracting(
                        Map::keySet
                )
                .containsExactlyInAnyOrder(
                        Set.of(LogField.TRACE_ID.getKey())
                );
        assertThat(actual)
                .isNotNull()
                .extracting(
                        OrdersLocationMessage::orderLocations
                           )
                .usingRecursiveComparison()
                .isEqualTo(message.orderLocations());
    }
}
