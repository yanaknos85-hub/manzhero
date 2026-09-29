package ru.sberbank.ditsib.corpclient.messaging.listener;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sber.transport.user_data_confirmation.message.UserDataConfirmationMessage;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.service.ContactConfirmationService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка слушателей")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Активация в канале SCIM")
    void test_activationFromScim() {
        var employeeService = mock(EmployeeService.class);

        var input = config.accountsRolesInput(employeeService);

        var message = Instancio.of(AccountRoleLinkMessage.class)
                .set(Select.field(AccountRoleLinkMessage::getId), UUID.randomUUID().toString())
                .set(Select.field(AccountRoleLinkMessage::isActive), true)
                .create();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), UUID.fromString(message.getId()))
                .set(Select.field(Employee::getActiveStatus), ActiveStatus.INACTIVE)
                .create();

        when(employeeService.getEmployeeByUserId(UUID.fromString(message.getId()))).thenReturn(employee);

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(rawMessage);

        var employeeCaptor = ArgumentCaptor.forClass(UUID.class);
        var rolesCaptor = ArgumentCaptor.forClass(AccountRoleLinkMessage.class);

        verify(employeeService).update(employeeCaptor.capture(), rolesCaptor.capture());

        var actual = employeeCaptor.getValue();
        assertThat(actual).isEqualTo(employee.getId());

        var roles = rolesCaptor.getValue();
        assertThat(roles.getRoles()).hasSameElementsAs(message.getRoles());
    }

    @Test
    @DisplayName("Подтверждение")
    void test_confirmationData() {
        var contactConfirmationService = mock(ContactConfirmationService.class);

        var input = config.confirmationDataInput(contactConfirmationService);

        var message = Instancio.of(UserDataConfirmationMessage.class)
                .set(Select.field(UserDataConfirmationMessage::getId), UUID.randomUUID())
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(rawMessage);

        verify(contactConfirmationService)
                .confirm(message);
    }

    @Test
    @DisplayName("Подтверждение")
    void test_confirmationData_PhoneIsNull() {
        var contactConfirmationService = mock(ContactConfirmationService.class);

        var input = config.confirmationDataInput(contactConfirmationService);

        var message = Instancio.of(UserDataConfirmationMessage.class)
                .set(Select.field(UserDataConfirmationMessage::getId), UUID.randomUUID())
                .ignore(Select.field(UserDataConfirmationMessage::getPhone))
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(rawMessage);

        verify(contactConfirmationService, never())
                .confirm(any());
    }

    @Test
    @DisplayName("Подтверждение")
    void test_confirmationData_IdIsNull() {
        var contactConfirmationService = mock(ContactConfirmationService.class);

        var input = config.confirmationDataInput(contactConfirmationService);

        var message = Instancio.of(UserDataConfirmationMessage.class)
                .ignore(Select.field(UserDataConfirmationMessage::getId))
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, UUID.randomUUID())));

        input.accept(rawMessage);

        verify(contactConfirmationService, never())
                .confirm(any());
    }

    @Test
    @DisplayName("Деактивация в канале SCIM")
    void test_deactivationFromScim() {
        var employeeService = mock(EmployeeService.class);

        var input = config.accountsRolesInput(employeeService);

        var message = Instancio.of(AccountRoleLinkMessage.class)
                .set(Select.field(AccountRoleLinkMessage::getId), UUID.randomUUID().toString())
                .set(Select.field(AccountRoleLinkMessage::isActive), false)
                .create();

        var employee = Instancio.of(Employee.class)
                .set(Select.field(Employee::getId), UUID.fromString(message.getId()))
                .set(Select.field(Employee::getActiveStatus), ActiveStatus.ACTIVE)
                .create();

        when(employeeService.getEmployeeByUserId(UUID.fromString(message.getId()))).thenReturn(employee);

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(rawMessage);

        var employeeCaptor = ArgumentCaptor.forClass(UUID.class);
        var rolesCaptor = ArgumentCaptor.forClass(AccountRoleLinkMessage.class);

        verify(employeeService).update(employeeCaptor.capture(), rolesCaptor.capture());

        var actual = employeeCaptor.getValue();
        assertThat(actual).isEqualTo(employee.getId());

        var roles = rolesCaptor.getValue();
        assertThat(roles.getRoles()).hasSameElementsAs(message.getRoles());
        assertThat(roles.isActive()).isFalse();
    }

}