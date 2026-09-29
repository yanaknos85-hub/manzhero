package ru.sber.transport.integrations.utils;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Компараторы")
class ComparatorUtilsTest {

    @DisplayName("Проверка сортировки статусов")
    @Test()
    void test() {
        var queue = new PriorityBlockingQueue<>(8, ComparatorUtils.compareOutContractorTaxiTripMessage());
        var payload1 = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                .create();
        var payload2 = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.IN_PROGRESS)
                .create();
        var payload3 = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.NEW)
                .create();
        var payload4 = Instancio.of(OutContractorTaxiTripMessage.class)
                .set(field(OutContractorTaxiTripMessage::status), OutboundRequestStatus.REJECT)
                .create();

        queue.add(MessageBuilder.createMessage(payload1, new MessageHeaders(Collections.emptyMap())));
        queue.add(MessageBuilder.createMessage(payload2, new MessageHeaders(Collections.emptyMap())));
        queue.add(MessageBuilder.createMessage(payload3, new MessageHeaders(Collections.emptyMap())));
        queue.add(MessageBuilder.createMessage(payload4, new MessageHeaders(Collections.emptyMap())));

        assertThat(queue).hasSize(4);
        assertThat(Optional.ofNullable(queue.poll()).orElseThrow().getPayload().status()).isEqualTo(OutboundRequestStatus.NEW);
        assertThat(Optional.ofNullable(queue.poll()).orElseThrow().getPayload().status()).isEqualTo(OutboundRequestStatus.REJECT);
        assertThat(Optional.ofNullable(queue.poll()).orElseThrow().getPayload().status()).isEqualTo(OutboundRequestStatus.IN_PROGRESS);
        assertThat(Optional.ofNullable(queue.poll()).orElseThrow().getPayload().status()).isEqualTo(OutboundRequestStatus.IN_PROGRESS);
    }

}
