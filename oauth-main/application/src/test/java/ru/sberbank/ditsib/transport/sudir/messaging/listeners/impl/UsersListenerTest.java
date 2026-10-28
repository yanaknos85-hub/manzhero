package ru.sberbank.ditsib.transport.sudir.messaging.listeners.impl;

import io.qameta.allure.Feature;
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
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка получения аккаунтов")
@ActiveProfiles("test")
class UsersListenerTest {

    @MockitoSpyBean
    private Accounts accounts;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Consumer<Message<UserMessage>> usersInput;

    @DisplayName("Новый пользователь. Внутренний")
    @Test
    void handleUsers_newUser_internal() {
        var message = createUserMessage();

        usersInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of("type", "INTERNAL",
                KafkaHeaders.RECEIVED_KEY, message.id())
        )));

        var accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accounts).save(accountArgumentCaptor.capture());

        var actual = accountArgumentCaptor.getValue();
        assertThat(actual).isInstanceOf(Account.class);
        assertThat(actual.getPersonnelNumber()).isEqualTo(message.personnelNumber());
        assertThat(actual.getId()).isEqualTo(message.getId());
    }

    @Transactional
    @DisplayName("Редактирование пользователя с новыми ролями")
    @Test
    void handleUsers_editUser() {
        Map<String, Boolean> collect = IntStream.of(3).boxed().collect(Collectors.toMap(value -> "Role" + value, value -> Boolean.FALSE));
        collect.forEach((key, value1) -> roleRepository.save(createRole(key, value1)));
        UUID id = UUID.randomUUID();
        accounts.save(Account.builder()
                .id(id)
                .personnelNumber("1245")
                .active(true)
                .roles(collect).build());

        Set<String> collect1 = IntStream.of(4, 10).boxed().map(value -> "Role" + value).collect(Collectors.toSet());
        collect1.forEach(key -> roleRepository.save(createRole(key, true)));
        var message = createUserMessage(id, "1245", collect1);
        usersInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of("type", "INTERNAL",
                KafkaHeaders.RECEIVED_KEY, message.id())
        )));

        verify(accounts, times(2)).save(any());
        var byActiveTrueAndLogin = accountRepository.findByActiveTrueAndPersonnelNumber(message.personnelNumber());
        assertThat(byActiveTrueAndLogin).isPresent()
                .hasValueSatisfying(account -> assertThat(account.getPersonnelNumber()).isEqualTo(message.personnelNumber()))
                .hasValueSatisfying(account -> assertThat(account.getId()).isEqualTo(message.getId()));
    }

    private UserMessage createUserMessage(UUID id, String personnelNumber, Set<String> roles) {
        return new UserMessage(
                id,
                null,
                null,
                null,
                personnelNumber,
                null,
                null,
                true,
                false,
                true,
                roles,
                false,
                null,
                "INTERNAL",
                null,
                null
        );
    }

    private UserMessage createUserMessage() {
        return createUserMessage(UUID.randomUUID(), "personnelNumber", null);
    }

    private RoleRecord createRole(String key, boolean dataMaster) {
        var role = new RoleRecord();
        role.setCode(key);
        role.setName(key);
        role.setDescription(key);
        role.setDataMaster(dataMaster);
        return role;
    }
}