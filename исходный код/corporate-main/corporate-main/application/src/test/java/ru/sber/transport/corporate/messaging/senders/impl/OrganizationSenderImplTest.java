package ru.sber.transport.corporate.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Contact;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate.messaging.senders.OrganizationSender;
import ru.sber.transport.corporate.messaging.senders.mappers.OrganizationMessageMapperImpl;
import ru.sber.transport.messages.corporate.avro.ContactMessage;
import ru.sber.transport.messages.corporate.avro.ContactType;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.SimpleObjectProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка отправителя сообщений организации")
class OrganizationSenderImplTest {

    private final SimpleObjectProvider<OutputBridge> plaintextProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> sslProvider = new SimpleObjectProvider<>(null);

    private final SimpleObjectProvider<OutputBridge> avroProvider = new SimpleObjectProvider<>(null);

    private final OrganizationSender sender = new OrganizationSenderImpl(plaintextProvider, sslProvider, avroProvider, new OrganizationMessageMapperImpl(new ActiveMessageMapperImpl()));

    @Test
    @DisplayName("Отправка полного сообщения")
    void test_send_avro_full() throws InterruptedException, ExecutionException {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var source = Instancio.create(Organization.class);

        sender.send(source).get();

        var messageCaptor = ArgumentCaptor.forClass(OrganizationMessage.class);

        verify(bridge).send(messageCaptor.capture(), eq(Map.of("syncId", source.getSyncId())));

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getAddress()).isEqualTo(source.getAddress());
            it.assertThat(value.getCode()).isEqualTo(source.getCode());
            it.assertThat(value.getName()).isEqualTo(source.getName());
            it.assertThat(value.getMsrn()).isEqualTo(source.getMsrn());
            it.assertThat(value.getDeleted()).isEqualTo(source.getStatus() == Active.INACTIVE);
            it.assertThat(value.getTid()).isEqualTo(source.getTid());
            it.assertThat(value.getDigitId()).isEqualTo(source.getDigitId());
            it.assertThat(value.getContacts()).hasSameSizeAs(source.getContacts());
            it.assertThat(value.getContacts().stream().map(ContactMessage::getId).toList()).hasSameElementsAs(source.getContacts().stream().map(Contact::getId).toList());
            it.assertThat(value.getContacts().stream().map(ContactMessage::getValue).toList()).hasSameElementsAs(source.getContacts().stream().map(Contact::getValue).toList());
            it.assertThat(value.getContacts().stream().map(ContactMessage::getType).map(ContactType::name).toList()).hasSameElementsAs(source.getContacts().stream().map(Contact::getType).map(Enum::name).toList());
        });
    }

    @Test
    @DisplayName("Отправка минимального сообщения")
    void test_send_avro_min() {
        var bridge = mock(OutputBridge.class);

        avroProvider.set(bridge);

        var source = new Organization();
        source.setId(UUID.randomUUID());
        source.setDigitId(Instancio.create(Integer.class));
        source.setName(Instancio.create(String.class));
        source.setAddress(Instancio.create(String.class));
        source.setMsrn(Instancio.create(String.class));
        source.setTid(Instancio.create(String.class));

        sender.send(source);

        var messageCaptor = ArgumentCaptor.forClass(OrganizationMessage.class);

        verify(bridge).send(messageCaptor.capture(), eq(Map.of()));

        var value = messageCaptor.getValue();
        assertThat(value).isNotNull();
        assertSoftly(it -> {
            it.assertThat(value.getAddress()).isEqualTo(source.getAddress());
            it.assertThat(value.getCode()).isEqualTo(source.getCode());
            it.assertThat(value.getName()).isEqualTo(source.getName());
            it.assertThat(value.getMsrn()).isEqualTo(source.getMsrn());
            it.assertThat(value.getDeleted()).isEqualTo(source.getStatus() == Active.INACTIVE);
            it.assertThat(value.getTid()).isEqualTo(source.getTid());
            it.assertThat(value.getDigitId()).isEqualTo(source.getDigitId());
        });
    }

}