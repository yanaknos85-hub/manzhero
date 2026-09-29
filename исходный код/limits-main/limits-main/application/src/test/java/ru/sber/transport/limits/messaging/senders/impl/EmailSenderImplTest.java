package ru.sber.transport.limits.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.limits.SimpleObjectProvider;
import ru.sber.transport.limits.messaging.senders.EmailSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка отправки сообщения об истекающем лимите")
class EmailSenderImplTest {

    private final ObjectProvider<OutputBridge> emailOutput = new SimpleObjectProvider<>(mock(OutputBridge.class));
    private final ObjectProvider<OutputBridge> emailOutputSsl = new SimpleObjectProvider<>(null);

    private final EmailSender emailSender = new EmailSenderImpl(emailOutput, emailOutputSsl);

    @Test
    @DisplayName("Проверка отправки сообщения об истекающем лимите")
    void test_send() {
        ((EmailSenderImpl) emailSender).setSubject("test subject");
        ((EmailSenderImpl) emailSender).setTemplatePath("src/test/resources/email/limit-ends.html");

        emailSender.send(List.of("test@test.ru"), "department", "limit", 10);

        var messageCaptor = ArgumentCaptor.forClass(EmailMessage.class);

        verify(emailOutput.getIfAvailable()).send(messageCaptor.capture());

        var email = messageCaptor.getValue();
        assertThat(email).isNotNull();
        assertSoftly(it -> {
            it.assertThat(email.getSubject()).isEqualTo("test subject");
            it.assertThat(email.getData()).containsEntry("percent", 10);
            it.assertThat(email.getData()).containsEntry("departmentId", "department");
            it.assertThat(email.getData()).containsEntry("limitId", "limit");
            it.assertThat(email.getTemplate()).isEqualTo("test file template");
            it.assertThat(email.isHtml()).isTrue();
        });
    }

}