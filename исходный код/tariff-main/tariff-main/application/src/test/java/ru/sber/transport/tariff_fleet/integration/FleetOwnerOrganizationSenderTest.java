package ru.sber.transport.tariff_fleet.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.messaging.sender.FleetOwnerOrganizationSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FleetOwnerOrganizationMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
class FleetOwnerOrganizationSenderTest extends KafkaTest {
    @Autowired
    private FleetOwnerOrganizationSender sender;
    
    @Test
    @SneakyThrows
    void send() {
        var message = Instancio.create(FleetOwnerOrganizationMessage.class);
        sender.send(message);
        var actual = consumeMessage("service.tariff-fleet.fleet_owner_organization", FleetOwnerOrganizationMessage.class);
        assertThat(actual)
                .isNotNull()
                .extracting(FleetOwnerOrganizationMessage::id,
                            FleetOwnerOrganizationMessage::organizationId,
                            FleetOwnerOrganizationMessage::edfOperatorId,
                            FleetOwnerOrganizationMessage::edfCode)
                .containsExactly(message.id(),
                                 message.organizationId(),
                                 message.edfOperatorId(),
                                 message.edfCode());
    }
}