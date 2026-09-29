package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class EwbTariffSenderTest extends KafkaTest {
    @Autowired
    private EwbTariffSender sender;
    
    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(EwbTariffMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.ewb_tariff", EwbTariffMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(EwbTariffMessage::id,
                            EwbTariffMessage::tariffId,
                            EwbTariffMessage::contractId,
                            EwbTariffMessage::organizationId,
                            EwbTariffMessage::departmentId,
                            EwbTariffMessage::active)
                .containsExactly(message.id(),
                                 message.tariffId(),
                                 message.contractId(),
                                 message.organizationId(),
                                 message.departmentId(),
                                 message.active());
    }
}