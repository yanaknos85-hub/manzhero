package ru.sberbank.ditsib.transport.sudir.messaging.sender.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.SimpleObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.user.agent.messages.UserAgentMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка работы отправителя данных")
class UserAgentSenderImplTest {

    private final OutputBridge bridge = mock(OutputBridge.class);

    private final UserAgentSender sender = new UserAgentSenderImpl(new SimpleObjectProvider<>(bridge), new SimpleObjectProvider<>(null));

    @Test
    @DisplayName("Проверка отправки")
    void test_send() {
        var account = Instancio.create(Account.class);
        var userAgent = Instancio.create(String.class);
        var clientType = Instancio.create(String.class);
        var requestUrl = Instancio.create(String.class);

        sender.send(account, userAgent, clientType, requestUrl);

        var message = ArgumentCaptor.forClass(UserAgentMessage.class);

        verify(bridge).send(message.capture());

        var actual = message.getValue();
        assertThat(actual).isNotNull();

        assertSoftly(soft -> {
            soft.assertThat(actual.getAuthType()).isEqualTo("SUDIR");
            soft.assertThat(actual.getUserId()).isEqualTo(account.getId());
        });
    }

}