package ru.sber.transport.messaging.kafka.binding;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_kafka_functional")
@DisplayName("Проверка моста отправки сообщений")
class OutputBridgeTest {

    private final StreamBridge streamBridge = mock(StreamBridge.class);

    private final OutputBridge outputBridge = new OutputBridge(streamBridge) {
        @Override
        public String channelId() {
            return "test channel";
        }
    };

    @Test
    @DisplayName("Отправка сообщения")
    void test_sendMessage() throws ExecutionException, InterruptedException {
        var message = new TestMessage(UUID.randomUUID(), "Some text");
        var future = outputBridge.send(message);
        future.get();

        var messageCaptor = ArgumentCaptor.forClass(org.springframework.messaging.Message.class);

        verify(streamBridge).send(eq("test channel"), messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isNotNull();
        assertThat(messageCaptor.getValue().getPayload()).isInstanceOf(TestMessage.class);
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry(KafkaHeaders.KEY, message.id);
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry("contentType", "application/json");
        assertThat(((TestMessage) messageCaptor.getValue().getPayload()).id).isEqualTo(message.id);
        assertThat(((TestMessage) messageCaptor.getValue().getPayload()).string).isEqualTo(message.string);
    }

    @Test
    @DisplayName("Отправка сообщения авро")
    void test_sendMessage_avro() throws ExecutionException, InterruptedException {
        var message = new ru.sber.transport.avro.messages.TestMessage(UUID.randomUUID(), "Some text");
        var future = outputBridge.send(message);
        future.get();

        var messageCaptor = ArgumentCaptor.forClass(org.springframework.messaging.Message.class);

        verify(streamBridge).send(eq("test channel"), messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isNotNull();
        assertThat(messageCaptor.getValue().getPayload()).isInstanceOf(ru.sber.transport.avro.messages.TestMessage.class);
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry(KafkaHeaders.KEY, message.getId());
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry("contentType", "application/*+avro");
        assertThat(((ru.sber.transport.avro.messages.TestMessage) messageCaptor.getValue().getPayload()).getId()).isEqualTo(message.getId());
        assertThat(((ru.sber.transport.avro.messages.TestMessage) messageCaptor.getValue().getPayload()).getText()).isEqualTo(message.getText());
    }

    @Test
    @DisplayName("Отправка сообщения. Список")
    void test_sendMessage_list() throws ExecutionException, InterruptedException {
        var message = new TestMessage(UUID.randomUUID(), "Some text");
        var future = outputBridge.send(List.of(message));
        future.get();

        var messageCaptor = ArgumentCaptor.forClass(org.springframework.messaging.Message.class);

        verify(streamBridge).send(eq("test channel"), messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isNotNull();
        assertThat(messageCaptor.getValue().getPayload()).isInstanceOf(List.class);
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry(KafkaHeaders.BATCH_CONVERTED_HEADERS, List.of(Map.of("contentType", "application/json", KafkaHeaders.KEY, message.id)));
        assertThat(((TestMessage) ((List<?>) messageCaptor.getValue().getPayload()).get(0)).id).isEqualTo(message.id);
        assertThat(((TestMessage) ((List<?>) messageCaptor.getValue().getPayload()).get(0)).string).isEqualTo(message.string);
    }

    @Test
    @DisplayName("Отправка сообщения авро. Список")
    void test_sendMessage_avro_list() throws ExecutionException, InterruptedException {
        var message = new ru.sber.transport.avro.messages.TestMessage(UUID.randomUUID(), "Some text");
        var future = outputBridge.send(List.of(message));
        future.get();

        var messageCaptor = ArgumentCaptor.forClass(org.springframework.messaging.Message.class);

        verify(streamBridge).send(eq("test channel"), messageCaptor.capture());

        assertThat(messageCaptor.getValue()).isNotNull();
        assertThat(messageCaptor.getValue().getPayload()).isInstanceOf(List.class);
        assertThat(messageCaptor.getValue().getHeaders()).containsEntry(KafkaHeaders.BATCH_CONVERTED_HEADERS, List.of(Map.of("contentType", "application/*+avro", KafkaHeaders.KEY, message.getId())));
        assertThat(((ru.sber.transport.avro.messages.TestMessage) ((List<?>) messageCaptor.getValue().getPayload()).get(0)).getId()).isEqualTo(message.getId());
        assertThat(((ru.sber.transport.avro.messages.TestMessage) ((List<?>) messageCaptor.getValue().getPayload()).get(0)).getText()).isEqualTo(message.getText());
    }

    record TestMessage(
        UUID id,
        String string
    ) implements Message<UUID>{
        @Override
        public UUID getId() {
            return id;
        }
    }

}