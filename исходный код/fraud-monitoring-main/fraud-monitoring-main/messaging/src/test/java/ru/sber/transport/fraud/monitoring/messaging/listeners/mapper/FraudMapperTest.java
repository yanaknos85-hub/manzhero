package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudMessagePlain;
import ru.sber.transport.messages.ai_receipt_scanner.avro.FraudMessage;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест маппера сообщений о мошенничестве")
class FraudMapperTest {

    private final FraudMapper fraudMapper = new FraudMapperImpl();

    @Test
    void toFraud() {
        var message = Instancio.create(FraudMessagePlain.class);
        var result = fraudMapper.toFraud(message);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getRequestId()).isEqualTo(message.id());
        assertThat(result.getComment()).isEqualTo(message.comment());
    }

    @Test
    void receiptFraudAvroMessageToFraud() {
        var message = Instancio.create(FraudMessage.class);
        var result = fraudMapper.receiptFraudAvroMessageToFraud(message);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getRequestId()).isEqualTo(message.getId());
        assertThat(result.getComment()).isEqualTo(message.getComment());
    }

    @Test
    void radiusFraudAvroMessageToFraud() {
        var message = Instancio.create(ru.sber.transport.messages.fraud.avro.FraudMessage.class);
        var result = fraudMapper.radiusFraudAvroMessageToFraud(message);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getRequestId()).isEqualTo(message.getId());
        assertThat(result.getComment()).isEqualTo(message.getComment());
    }
}