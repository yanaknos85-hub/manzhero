package ru.sber.transport.fraud.monitoring.messaging.listeners;

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
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

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
class EmployeesProviderListenerTest {

    private final EmployeesService employeesService = mock(EmployeesService.class);

    private final Consumer<Message<EmployeeMessage>> input = new EmployeeListener(employeesService);


    @Test
    @DisplayName("Получение данных сотрудника")
    void test() {
        final var message = EmployeeMessage.builder()
                .humanReadableId(Instancio.create(String.class))
                .firstName(Instancio.create(String.class))
                .lastName(Instancio.create(String.class))
                .patronymic(Instancio.create(String.class))
                .personnelNumber(Instancio.create(String.class))
                .deleted(Instancio.create(Boolean.class))
                .consent(Instancio.create(Boolean.class))
                .employeeType("INTERNAL")
                .positionId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .id(UUID.randomUUID())
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