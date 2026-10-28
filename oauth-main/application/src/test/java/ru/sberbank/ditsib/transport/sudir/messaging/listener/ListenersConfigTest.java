package ru.sberbank.ditsib.transport.sudir.messaging.listener;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.messages.EmployeeRoleMessage;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.messaging.mappers.AccountMessageMapperImpl;
import ru.sberbank.ditsib.transport.sudir.messaging.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SuppressWarnings("unchecked")
@DisplayName("Проверка слушателей")
class ListenersConfigTest {

    private final ListenersConfig config = new ListenersConfig();

    @Test
    @DisplayName("Проверка получения пользователей")
    void test_usersInput() {
        var accountProcessor = mock(Accounts.class);
        var consumer = config.usersInput(accountProcessor, new AccountMessageMapperImpl());
        var messageInternal = Instancio.of(UserMessage.class)
                .set(field(UserMessage::orgStructureType), "INTERNAL")
                .create();
        var rawMessageInternal = MessageBuilder.createMessage(messageInternal, new MessageHeaders(Map.of("type", "INTERNAL")));
        var messageExternal = Instancio.of(UserMessage.class)
                .set(field(UserMessage::orgStructureType), "EXTERNAL")
                .create();
        var rawMessageExternal = MessageBuilder.createMessage(messageExternal, new MessageHeaders(Map.of("type", "EXTERNAL")));
        consumer.accept(rawMessageInternal);
        consumer.accept(rawMessageExternal);

        var usersCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountProcessor, times(1)).save(usersCaptor.capture());

        var actualValues = usersCaptor.getAllValues();
        assertThat(actualValues).hasSize(1);

        assertSoftly(it -> {
           it.assertThat(actualValues.getFirst().getPersonnelNumber()).isEqualTo(messageInternal.personnelNumber());
           it.assertThat(actualValues.getFirst().getId()).isEqualTo(messageInternal.id());
        });
    }

    @Test
    @DisplayName("Добавление роли")
    void test_role_add() {
        var provider = mock(RoleProvider.class);

        var consumer = config.rolesInput(provider);

        var message = Instancio.of(RoleMessage.class)
                .set(field(RoleMessage::deleted), false)
                .set(field(RoleMessage::exclusives), List.of("INTERNAL"))
                .create();

        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.code()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        consumer.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);

        verify(provider).save(messageCaptor.capture());

        var actual = messageCaptor.getValue();

        assertThat(actual.deleted()).isEqualTo(message.deleted());
        assertThat(actual.code()).isEqualTo(message.code());
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.name()).isEqualTo(message.name());
    }

    @Test
    @DisplayName("Удаление роли")
    void test_role_delete() {
        var provider = mock(RoleProvider.class);

        var consumer = config.rolesInput(provider);

        var message = Instancio.of(RoleMessage.class)
                .set(field(RoleMessage::deleted), true)
                .create();

        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.code()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        consumer.accept(rawMessage);

        var messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(provider).delete(messageCaptor.capture());

        var actual = messageCaptor.getValue();

        assertThat(actual).isEqualTo(message.code());
    }

    @Test
    @DisplayName("Связь роли и сотрудника")
    void test_accountRolesInput() {
        var cases = mock(Accounts.class);

        var consumer = config.accountsRolesInput(cases);

        var message = Instancio.of(AccountRoleLinkMessage.class)
                .set(field(AccountRoleLinkMessage::getId), UUID.randomUUID().toString())
                .create();
        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        consumer.accept(rawMessage);

        var idCaptor = ArgumentCaptor.forClass(UUID.class);
        var rolesCaptor = ArgumentCaptor.forClass(Set.class);
        var personnelNumbersCaptor = ArgumentCaptor.forClass(String.class);
        var activationCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(cases).activation(idCaptor.capture(), personnelNumbersCaptor.capture(), rolesCaptor.capture(), activationCaptor.capture());

        var actualId = idCaptor.getValue();
        var actualPersonnelNumber = personnelNumbersCaptor.getValue();
        var actualRoles = rolesCaptor.getValue();
        var activation = activationCaptor.getValue();

        assertThat(actualId).isEqualTo(UUID.fromString(message.getId()));
        assertThat(actualPersonnelNumber).isEqualTo(message.getPersonnelNumber());
        assertThat(actualRoles).hasSameElementsAs(message.getRoles());
        assertThat(activation).isEqualTo(message.isActive());
    }

    @Test
    @DisplayName("Связь роли и сотрудника. Неверный ID")
    void test_accountRolesInput_wrongId() {
        var cases = mock(Accounts.class);

        var consumer = config.accountsRolesInput(cases);

        var message = Instancio.of(AccountRoleLinkMessage.class)
                .create();
        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        consumer.accept(rawMessage);

        verify(cases, never()).activation(any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Связь роли и сотрудника. Неверный ID")
    void test_employeeRoleInput() {
        var accountRepository = mock(AccountRepository.class);

        var roleRepository = mock(RoleRepository.class);

        var consumer = config.employeeRoleInput(accountRepository, roleRepository);

        var message = Instancio.of(EmployeeRoleMessage.class)
                .create();
        var headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()));

        var rawMessage = MessageBuilder.createMessage(message, headers);

        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(new AccountRecord()));
        when(roleRepository.findById(any(String.class))).thenReturn(Optional.of(new RoleRecord()));

        consumer.accept(rawMessage);

        verify(roleRepository).addRole(any(), any());
    }

}