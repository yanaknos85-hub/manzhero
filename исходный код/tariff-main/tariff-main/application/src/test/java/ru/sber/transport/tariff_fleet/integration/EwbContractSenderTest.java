package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class EwbContractSenderTest extends KafkaTest {
    @Autowired
    private EwbContractSender sender;
    
    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(EwbContractMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.ewb_contract", EwbContractMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(EwbContractMessage::id,
                            EwbContractMessage::contractId,
                            EwbContractMessage::organizationId,
                            EwbContractMessage::inspectionType,
                            EwbContractMessage::edfOperatorId,
                            EwbContractMessage::edfCode,
                            EwbContractMessage::organizationMedicalLicenseId,
                            EwbContractMessage::start,
                            EwbContractMessage::end,
                            EwbContractMessage::active)
                .containsExactly(message.id(),
                                 message.contractId(),
                                 message.organizationId(),
                                 message.inspectionType(),
                                 message.edfOperatorId(),
                                 message.edfCode(),
                                 message.organizationMedicalLicenseId(),
                                 message.start(),
                                 message.end(),
                                 message.active());
    }
}