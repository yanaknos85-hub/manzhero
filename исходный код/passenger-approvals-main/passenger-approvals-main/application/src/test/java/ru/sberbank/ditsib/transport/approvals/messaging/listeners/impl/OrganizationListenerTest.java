package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.approvals.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
class OrganizationListenerTest extends KafkaTest {
    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    @Sql(scripts = {"/scripts/cleanup_database.sql", "/scripts/basic_corp_structure.sql"})
    @SneakyThrows
    void listen() {
        var organization1 = new Organization(UUID.randomUUID(),
                1L,
                true);
        var organization2 = new Organization(UUID.randomUUID(),
                2L,
                true);
        var organization3 = new Organization(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                8L,
                true);
        var organization4 = new Organization(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                11L,
                true);
        var organization5 = new Organization(UUID.fromString("11501599-b498-4e9c-8b75-3e5899c445c0"),
                77L,
                true
        );
        produceMessage("service.organization",
                new ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage(organization1.getId(),
                        organization1.getDigitId(),
                        Instancio.create(String.class),
                        null,
                        Instancio.create(String.class),
                        Instancio.create(String.class),
                        null,
                        Collections.emptyList(),
                        false,
                        new ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage.OrganizationGroup(UUID.randomUUID(),
                                Instancio.create(String.class),
                                true)
                ));
        produceMessage("service.organization",
                new ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage(organization2.getId(),
                        organization2.getDigitId(),
                        Instancio.create(String.class),
                        null,
                        Instancio.create(String.class),
                        Instancio.create(String.class),
                        null,
                        Collections.emptyList(),
                        false,
                        null));
        produceMessage("service.organization",
                new OrganizationMessage(organization3.getId(),
                        organization3.getDigitId(),
                        Instancio.create(String.class),
                        null,
                        Instancio.create(String.class),
                        Instancio.create(String.class),
                        null,
                        Collections.emptyList(),
                        false,
                        null));
        var actualOrganizations = organizationRepository.findAll();
        assertThat(actualOrganizations)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(organization1, organization2, organization3, organization4, organization5));
    }
}