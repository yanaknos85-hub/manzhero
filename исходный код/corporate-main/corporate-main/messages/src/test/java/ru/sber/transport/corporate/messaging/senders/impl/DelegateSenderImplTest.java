package ru.sber.transport.corporate.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.messaging.senders.DelegateSender;
import ru.sber.transport.corporate.model.Delegate;
import ru.sber.transport.messages.corporate.avro.DelegateData;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправителя делегатов")
class DelegateSenderImplTest {

    private final OutputBridge bridge = mock(OutputBridge.class);

    private final ObjectProvider<OutputBridge> bridgeProvider = new ObjectProvider<>() {
        @NotNull
        @Override
        public OutputBridge getObject() throws BeansException {
            return bridge;
        }
    };

    private final DelegateSender sender = new DelegateSenderImpl(bridgeProvider);

    @Test
    @DisplayName("Проверка отправки делегата")
    void test_send() {
        final var delegate = Instancio.create(TestDelegate.class);

        sender.send(delegate);

        final var delegateCaptor = ArgumentCaptor.forClass(DelegateData.class);

        verify(bridge).send(delegateCaptor.capture());

        final var actual = delegateCaptor.getValue();

        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(delegate.id());
            it.assertThat(actual.getDelegateId()).isEqualTo(delegate.delegateId());
            it.assertThat(actual.getSupervisorId()).isEqualTo(delegate.supervisorId());
            it.assertThat(actual.getStartDate()).isEqualTo(delegate.startDate());
            it.assertThat(actual.getEndDate()).isEqualTo(delegate.endDate());
            it.assertThat(actual.getTransportType()).isEqualTo(delegate.type());
            it.assertThat(actual.getDeleted()).isEqualTo(delegate.deleted());
        });
    }

    private record TestDelegate(UUID id, UUID delegateId, UUID supervisorId, LocalDate startDate, LocalDate endDate, String type, UUID typeId, boolean deleted) implements Delegate {}

}