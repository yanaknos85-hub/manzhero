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
import ru.sber.transport.fraud.monitoring.business.DepartmentsService;
import ru.sber.transport.fraud.monitoring.model.Department;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;

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
class DepartmentsProviderAvroListenerTest {

    private final DepartmentsService departmentsService = mock(DepartmentsService.class);

    private final Consumer<Message<DepartmentMessage>> input = new DepartmentAvroListener(departmentsService);

    @Test
    @DisplayName("Получение подразделений")
    void test() {
        final var message = DepartmentMessage.newBuilder()
                .setHumanReadableId(Instancio.create(String.class))
                .setCode(Instancio.create(String.class))
                .setDeleted(Instancio.create(Boolean.class))
                .setHeadId(UUID.randomUUID())
                .setId(UUID.randomUUID())
                .setOrganizationId(UUID.randomUUID())
                .setName(Instancio.create(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Department.class);

        verify(departmentsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHeadId()).isEqualTo(message.getHeadId());
        assertThat(actual.getCode()).isEqualTo(message.getCode());
    }

}