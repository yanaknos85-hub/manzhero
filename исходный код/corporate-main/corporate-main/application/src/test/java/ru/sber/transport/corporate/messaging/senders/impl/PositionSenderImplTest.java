package ru.sber.transport.corporate.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.StructureType;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate.messaging.senders.PositionSender;
import ru.sber.transport.corporate.messaging.senders.mappers.PositionMessageMapperImpl;
import ru.sber.transport.messages.corporate.avro.PositionMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка сервиса отправки подразделений")
class PositionSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> avroProvider = new SimpleObjectProvider<>(null);

    private final PositionSender sender = new PositionSenderImpl(plaintextProvider, sslProvider, avroProvider, new PositionMessageMapperImpl(new ActiveMessageMapperImpl()));

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_minimum() {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var source = new Position(Instancio.create(String.class), Instancio.create(Active.class), Instancio.create(StructureType.class));
        source.setId(UUID.randomUUID());
        source.setOrganizationId(UUID.randomUUID());
        source.setNoApproveRequired(Instancio.create(Boolean.class));
        source.setHumanReadableId(Instancio.create(String.class));

        sender.send(source);

        var messageCaptor = ArgumentCaptor.forClass(PositionMessage.class);

        verify(bridge).send(messageCaptor.capture(), eq(Map.of()));

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getId()).isEqualTo(source.getId());
            it.assertThat(value.getSelfApproved()).isEqualTo(source.isNoApproveRequired());
            it.assertThat(value.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(value.getName()).isEqualTo(source.getName());
            it.assertThat(value.getActive()).isEqualTo(source.getStatus() == Active.ACTIVE);
            it.assertThat(value.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
        });
    }
}