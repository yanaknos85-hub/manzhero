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
import ru.sber.transport.fraud.monitoring.business.PositionsService;
import ru.sber.transport.fraud.monitoring.model.Position;
import ru.sber.transport.messages.corporate.avro.PositionMessage;

import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка получения данных должностей из кафки")
class PositionsProviderAvroListenerTest {

    private final PositionsService positionsService = mock(PositionsService.class);

    private final Consumer<Message<PositionMessage>> input = new PositionAvroListener(positionsService);

    @Test
    @DisplayName("Получение данных должностей")
    void test() {
        final var message = PositionMessage.newBuilder()
                .setHumanReadableId(Instancio.create(String.class))
                .setName(Instancio.create(String.class))
                .setId(UUID.randomUUID())
                .setActive(Instancio.create(Boolean.class))
                .setOrganizationId(UUID.randomUUID())
                .setSelfApproved(Instancio.create(Boolean.class))
                .setAvailableClasses(Instancio.createList(String.class))
                .build();

        input.accept(MessageBuilder.withPayload(message).build());

        final var messageCaptor = ArgumentCaptor.forClass(Position.class);

        verify(positionsService).createOrUpdate(messageCaptor.capture());

        final var actual = messageCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(message.getId());
            it.assertThat(actual.getName()).isEqualTo(message.getName());
        });
    }

}