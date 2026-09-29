package ru.sberbank.ditsib.corpclient.messaging.sender.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.corpclient.mapper.TransportOrgMapperImpl;
import ru.sberbank.ditsib.corpclient.messaging.sender.TransportOrgSender;
import ru.sberbank.ditsib.transport.messaging.messages.TransportOrgMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправителя сообщений сотрудников")
class TransportOrgSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final TransportOrgSender sender = new TransportOrgSenderImpl(plaintextProvider, sslProvider, new TransportOrgMapperImpl());

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_minimum() {
        var bridge = mock(OutputBridge.class);

        sslProvider.set(bridge);

        var source = TransportOrg.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .transportType("DOMESTIC_COURIER")
                .build();
        var deleted = false;

        sender.send(source, deleted);

        var messageCaptor = ArgumentCaptor.forClass(TransportOrgMessage.class);

        verify(bridge).send(messageCaptor.capture());

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getId()).isEqualTo(source.getId());
            it.assertThat(value.getOrganizationId()).isEqualTo(source.getOrganizationId());
            it.assertThat(value.getTransportType().name()).isEqualTo(source.getTransportType());
            it.assertThat(value.isDeleted()).isEqualTo(deleted);
        });
    }

}