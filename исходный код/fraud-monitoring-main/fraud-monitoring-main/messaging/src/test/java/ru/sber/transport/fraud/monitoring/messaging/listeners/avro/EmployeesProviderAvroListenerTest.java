package ru.sber.transport.fraud.monitoring.messaging.listeners.avro;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.EmployeesService;
import ru.sber.transport.fraud.monitoring.model.Employee;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.messages.corporate.avro.EmployeeType;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных сотрудников из кафки")
class EmployeesProviderAvroListenerTest {

    private final EmployeesService employeesService = mock(EmployeesService.class);

    private final Consumer<Message<EmployeeMessage>> input = new EmployeeAvroListener(employeesService);


    @Test
    @DisplayName("Получение данных сотрудника")
    void test() {
        final var message = EmployeeMessage.newBuilder()
                .setHumanReadableId(Instancio.create(String.class))
                .setFirstName(Instancio.create(String.class))
                .setLastName(Instancio.create(String.class))
                .setPatronymic(Instancio.create(String.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setDeleted(Instancio.create(Boolean.class))
                .setConsent(Instancio.create(Boolean.class))
                .setEmployeeType(Instancio.create(EmployeeType.class))
                .setPositionId(UUID.randomUUID())
                .setDepartmentId(UUID.randomUUID())
                .setOrganizationId(UUID.randomUUID())
                .setUserId(UUID.randomUUID())
                .setId(UUID.randomUUID())
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employeesService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDepartmentId()).isEqualTo(message.getDepartmentId());
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
        assertThat(actual.getFirstName()).isEqualTo(message.getFirstName());
        assertThat(actual.getLastName()).isEqualTo(message.getLastName());
        assertThat(actual.getPatronymic()).isEqualTo(message.getPatronymic());
        assertThat(actual.getPositionId()).isEqualTo(message.getPositionId());
    }

}