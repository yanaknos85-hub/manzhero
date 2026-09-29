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
import ru.sber.transport.fraud.monitoring.business.OrganizationsService;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных организации из кафки")
class OrganizationsProviderAvroListenerTest {

    private final OrganizationsService organizationsService = mock(OrganizationsService.class);

    private final Consumer<Message<OrganizationMessage>> input = new OrganizationAvroListener(organizationsService);

    @Test
    @DisplayName("Получение организаций")
    void test() {
        final var message = OrganizationMessage.newBuilder()
                .setAddress(Instancio.create(String.class))
                .setCode(Instancio.create(Integer.class))
                .setDeleted(Instancio.create(Boolean.class))
                .setGroup(UUID.randomUUID())
                .setId(UUID.randomUUID())
                .setDigitId(Instancio.create(Integer.class))
                .setName(Instancio.create(String.class))
                .setMsrn(Instancio.create(String.class))
                .setTid(Instancio.create(String.class))
                .setAvailableClasses(Instancio.createList(String.class))
                .setContacts(List.of())
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Organization.class);

        verify(organizationsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDigitId()).isEqualTo(message.getDigitId());
    }

}