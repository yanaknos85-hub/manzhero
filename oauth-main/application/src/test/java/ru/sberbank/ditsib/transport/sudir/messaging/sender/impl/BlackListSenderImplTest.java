package ru.sberbank.ditsib.transport.sudir.messaging.sender.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.SimpleObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.messages.BlackListMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.BlackListSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@EmbeddedPostgres
@DisplayName("Проверка отправки токена в ЧС")
class BlackListSenderImplTest {

    private final OutputBridge bridge = mock(OutputBridge.class);

    private final BlackListSender sender = new BlackListSenderImpl(new SimpleObjectProvider<>(bridge), new SimpleObjectProvider<>(null));
    
    @DisplayName("Отправка")
    @Test
    void test_send() {
        sender.send("Token");

        var message = ArgumentCaptor.forClass(BlackListMessage.class);
        verify(bridge).send(message.capture());

        assertThat(message.getValue()).isNotNull();
        
        assertThat(message.getValue().getToken()).isEqualTo("Token");
    }
    
}