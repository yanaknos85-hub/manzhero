package ru.sberbank.ditsib.transport.sudir.messaging.listeners.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка получения ролей")
@ActiveProfiles("test")
class RolesListenerTest {

    @MockitoSpyBean
    private RoleRepository roleRepository;

    @Autowired
    private Consumer<Message<RoleMessage>> rolesInput;

    @Test
    @DisplayName("Получена новая роль")
    void handleRole() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", List.of(), List.of("INTERNAL"), false, false);

        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        var messageCaptor = ArgumentCaptor.forClass(RoleRecord.class);

        verify(roleRepository).save(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.getCode()).isEqualTo(message.code());
        assertThat(actualMessage.getDescription()).isEqualTo(message.description());
        assertThat(actualMessage.getName()).isEqualTo(message.name());
    }

    @Test
    @DisplayName("Удалена роль")
    void handleRole_deleted() throws JsonProcessingException {
        when(roleRepository.findById(anyString())).thenReturn(Optional.of(createRole()));
        var message = new RoleMessage("CODE", "Name", "RoleDto description", List.of(), List.of(), false, true);
        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        var codeCaptor = ArgumentCaptor.forClass(RoleRecord.class);

        verify(roleRepository).delete(codeCaptor.capture());

        assertThat(codeCaptor.getValue().getCode()).isEqualTo(message.code());
    }

    private RoleRecord createRole() throws JsonProcessingException {
        return createRole(List.of());
    }

    private RoleRecord createRole(List<String> exclusions) throws JsonProcessingException {
        var role = new RoleRecord();
        role.setCode("CODE");
        role.setName("Name");
        role.setDefaultFor(JSON.valueOf(new ObjectMapper().writeValueAsString(exclusions)));
        role.setDescription(null);
        return role;
    }
}