package ru.sber.transport.tariff_fleet.integration;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairContractMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class RepairContractSenderTest extends KafkaTest {

    @Autowired
    private RepairContractSender sender;

    @Test
    void send() {
        var message = Instancio.create(RepairContractMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.repair_contract", RepairContractMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(RepairContractMessage::id,
                        RepairContractMessage::organizationId,
                        RepairContractMessage::number,
                        RepairContractMessage::uvhd,
                        RepairContractMessage::start,
                        RepairContractMessage::end,
                        RepairContractMessage::active,
                        RepairContractMessage::servicePointsName,
                        RepairContractMessage::amountWithoutVat,
                        RepairContractMessage::amountWithVat,
                        RepairContractMessage::contractorId,
                        RepairContractMessage::servicePoints)
                .containsExactly(message.id(),
                        message.organizationId(),
                        message.number(),
                        message.uvhd(),
                        message.start(),
                        message.end(),
                        message.active(),
                        message.servicePointsName(),
                        message.amountWithoutVat(),
                        message.amountWithVat(),
                        message.contractorId(),
                        message.servicePoints()
                );
    }
}
