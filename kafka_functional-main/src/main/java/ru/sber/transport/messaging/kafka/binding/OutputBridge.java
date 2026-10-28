package ru.sber.transport.messaging.kafka.binding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.messaging.Message;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Мост исходящего канала.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class OutputBridge implements org.springframework.beans.factory.SmartInitializingSingleton, Function<org.springframework.messaging.Message<?>, Future<Void>> {

    /**
     * Название заголовка с данными пользователя.
     */
    public static final String AUTHENTICATED_USER = "principal";

    private static final String SENDING_MESSAGE_TO_CHANNEL_FORMAT = "Sending message to channel {}";

    private static final String SENT_MESSAGE_TO_CHANNEL_FORMAT = "Sent message to channel {}";

    private static final String CONTENT_TYPE = "contentType";

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final StreamBridge bridge;

    /**
     * Отправить сообщение с заголовками по-умолчанию (аутентифицированный пользователь).
     *
     * @param data данные.
     * @param <T>  тип данных.
     * @return данные о задаче по отправке.
     */
    public <T> Future<Void> send(T data) {
        return send(data, Map.of());
    }

    /**
     * Отправить сообщение с заголовками.
     *
     * @param data              данные.
     * @param additionalHeaders дополнительные заголовки.
     * @param <T>               тип данных.
     * @return данные о задаче по отправке.
     */
    public <T> Future<Void> send(T data, Map<String, Object> additionalHeaders) {
        var headers = new HashMap<>(additionalHeaders);

        if (data instanceof Message<?> message) {
            appendJsonHeaders(message, headers);
        } else if (data instanceof SpecificRecord specificRecord) {
            appendAvroHeaders(specificRecord, headers);
        }

        Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .ifPresent(user -> headers.put(AUTHENTICATED_USER, user));

        return apply(MessageBuilder.createMessage(data, new MessageHeaders(headers)));
    }

    /**
     * Отправить сообщение с заголовками.
     *
     * @param data данные.
     * @param <T>  тип данных.
     * @return данные о задаче по отправке.
     */
    public <T> Future<Void> send(List<T> data) {
        return send(data, List.of());
    }

    /**
     * Отправить сообщение с заголовками.
     *
     * @param data              данные.
     * @param additionalHeaders дополнительные заголовки.
     * @param <T>               тип данных.
     * @return данные о задаче по отправке.
     */
    public <T> Future<Void> send(List<T> data, List<Map<String, Object>> additionalHeaders) {
        var sourceHeaders = new ArrayList<>(additionalHeaders);
        var headers = new HashMap<String, Object>();
        for (var i = 0; i < data.size(); i++) {
            var item = data.get(i);
            if (sourceHeaders.size() == i) {
                sourceHeaders.add(new HashMap<>());
            }
            if (item instanceof Message<?> message) {
                appendJsonHeaders(message, sourceHeaders.get(i));
            } else if (item instanceof SpecificRecord specificRecord) {
                appendAvroHeaders(specificRecord, sourceHeaders.get(i));
            }
        }
        headers.put(KafkaHeaders.BATCH_CONVERTED_HEADERS, sourceHeaders);

        Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .ifPresent(user -> headers.put(AUTHENTICATED_USER, user));

        var message = MessageBuilder.createMessage(data, new MessageHeaders(headers));

        return apply(message);
    }

    public Future<Void> apply(org.springframework.messaging.Message<?> message) {
        var channelId = channelId();
        return executor.submit(() -> {
            try {
                log.trace(SENDING_MESSAGE_TO_CHANNEL_FORMAT, channelId);
                bridge.send(channelId, message);
                log.debug(SENT_MESSAGE_TO_CHANNEL_FORMAT, channelId);
            } catch (Exception e) {
                log.warn("Sending message failed.", e);
            }
            return null;
        });
    }

    /**
     * @return Идентификатор канала.
     */
    public abstract String channelId();

    @Override
    public void afterSingletonsInstantiated() {
    }

    private static void appendJsonHeaders(Message<?> message, Map<String, Object> headers) {
        headers.put(KafkaHeaders.KEY, message.getId());
        headers.put(CONTENT_TYPE, "application/json");
    }

    private static void appendAvroHeaders(SpecificRecord specificRecord, Map<String, Object> headers) {
        specificRecord.getSchema().getFields()
            .stream()
            .filter(field -> field.aliases().contains("headerId"))
            .findFirst().ifPresent(f -> headers.put(KafkaHeaders.KEY, specificRecord.get(f.pos())));
        headers.put(CONTENT_TYPE, "application/*+avro");
    }
}
