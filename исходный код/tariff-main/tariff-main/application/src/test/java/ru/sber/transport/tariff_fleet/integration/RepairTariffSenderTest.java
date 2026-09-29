package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairTariffMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class RepairTariffSenderTest extends KafkaTest {

    @Autowired
    private RepairTariffSender sender;

    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(RepairTariffMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.repair_tariff", RepairTariffMessage.class);
        assertThat(actual)
            .isNotNull()
            .extracting(RepairTariffMessage::id,
                RepairTariffMessage::contractId,
                RepairTariffMessage::detailDiscountPrice,
                RepairTariffMessage::detailWarranty,
                RepairTariffMessage::hourNormalizedPrice,
                RepairTariffMessage::isFieldService,
                RepairTariffMessage::mileageWarranty,
                RepairTariffMessage::workWarranty,
                RepairTariffMessage::active,
                RepairTariffMessage::departmentId)
            .containsExactly(message.id(),
                message.contractId(),
                message.detailDiscountPrice(),
                message.detailWarranty(),
                message.hourNormalizedPrice(),
                message.isFieldService(),
                message.mileageWarranty(),
                message.workWarranty(),
                message.active(),
                message.departmentId());
    }

}
