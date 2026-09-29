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
import ru.sber.transport.fraud.monitoring.business.OrganizationsService;
import ru.sber.transport.fraud.monitoring.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных организации из кафки")
class OrganizationsProviderListenerTest {

    private final OrganizationsService organizationsService = mock(OrganizationsService.class);

    private final Consumer<Message<OrganizationMessage>> input = new OrganizationListener(organizationsService);

    @Test
    @DisplayName("Получение организаций")
    void test() {
        final var message = Instancio.create(OrganizationMessage.class);

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Organization.class);

        verify(organizationsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getDigitId()).isEqualTo(message.getDigitId());
    }

}