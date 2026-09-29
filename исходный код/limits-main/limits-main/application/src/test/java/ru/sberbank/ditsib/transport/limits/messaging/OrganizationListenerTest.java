package ru.sberbank.ditsib.transport.limits.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.limits.OldCommonTest;
import ru.sberbank.ditsib.transport.limits.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка получения организаций")
class OrganizationListenerTest extends OldCommonTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private Consumer<Message<OrganizationMessage>> organizationsInput;
    
    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Проверка получения новой организации")
    void handleOrganization_new() {
        var id = UUID.randomUUID();
        var idDigit = 1L;
        var message = new OrganizationMessage(id, idDigit, "Official name", "Address", "msrn", "tid", 0, List.of(), false, null);

        organizationsInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        
        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
        
    }
    
    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        repository.save(Organization.builder().id(id).digitId(1L).build());
        var message = new OrganizationMessage(id, 1L, "Official name", "Address", "msrn", "tid", 0, List.of(), true, null);

        organizationsInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        
        assertEquals(0, repository.findAllByActive(true).size());
    }
}