package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.OrganizationMedicalLicenseSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class OrganizationMedicalLicenseSenderTest extends KafkaTest {
    @Autowired
    private OrganizationMedicalLicenseSender sender;
    
    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(OrganizationMedicalLicenseMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.organization_medical_license", OrganizationMedicalLicenseMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(OrganizationMedicalLicenseMessage::id,
                            OrganizationMedicalLicenseMessage::series,
                            OrganizationMedicalLicenseMessage::number,
                            OrganizationMedicalLicenseMessage::issueDate,
                            OrganizationMedicalLicenseMessage::expiryDate)
                .containsExactly(message.id(),
                                 message.series(),
                                 message.number(),
                                 message.issueDate(),
                                 message.expiryDate());
    }
}