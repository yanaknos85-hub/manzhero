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
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных подразделений из кафки")
class DepartmentsProviderListenerTest {

    private final DepartmentsService departmentsService = mock(DepartmentsService.class);

    private final Consumer<Message<DepartmentMessage>> input = new DepartmentListener(departmentsService);

    @Test
    @DisplayName("Получение подразделений")
    void test() {
        final var message = DepartmentMessage.builder()
                .humanReadableId(Instancio.create(String.class))
                .code(Instancio.create(String.class))
                .deleted(Instancio.create(Boolean.class))
                .departmentHeadId(UUID.randomUUID())
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .departmentName(Instancio.create(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Department.class);

        verify(departmentsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHeadId()).isEqualTo(message.getDepartmentHeadId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
    }

}