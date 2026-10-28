package ru.sberbank.ditsib.transport.messaging.kafka.binding;

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
import ru.sberbank.ditsib.transport.messaging.Message;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Мост исходящего канала.
 * @deprecated Устарело. Использовать {@link ru.sber.transport.messaging.kafka.binding.OutputBridge}.
 */
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "3.11", forRemoval = true)
public abstract class OutputBridge implements org.springframework.beans.factory.SmartInitializingSingleton {

    /**
     * Название заголовка с данными пользователя.
     */
    public static final String AUTHENTICATED_USER = "principal";

    private static final String SENDING_MESSAGE_TO_CHANNEL_FORMAT = "Sending message to channel {}";

    private static final String SENT_MESSAGE_TO_CHANNEL_FORMAT = "Sent message to channel {}";

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final StreamBridge bridge;

    /**
     * Отправить сообщение с заголовками по-умолчанию (аутентифицированный пользователь).
     *
     * @param data данные.
     * @param <T>  тип данных.
     * @return данные о задаче по отправке.
     */
    public <T extends Message<?>> Future<Void> send(T data) {
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
    public <T extends Message<?>> Future<Void> send(T data, Map<String, Object> additionalHeaders) {
        var headers = new HashMap<>(additionalHeaders);

        Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .ifPresent(user -> headers.put(AUTHENTICATED_USER, user));

        headers.put(KafkaHeaders.KEY, data.getId());

        var message = MessageBuilder.createMessage(data, new MessageHeaders(headers));
        var channelId = channelId();
        log.trace(SENDING_MESSAGE_TO_CHANNEL_FORMAT, channelId);

        return doSend(message, channelId);
    }

    /**
     * Отправить сообщение с заголовками по-умолчанию (аутентифицированный пользователь).
     *
     * @param data данные.
     * @param <T>  тип данных.
     * @return данные о задаче по отправке.
     */
    public <T extends SpecificRecord> Future<Void> send(T data) {
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
    public <T extends SpecificRecord> Future<Void> send(T data, Map<String, Object> additionalHeaders) {
        var headers = new HashMap<>(additionalHeaders);

        data.getSchema().getFields()
                .stream()
                .filter(field -> field.aliases().contains("headerId"))
                .findFirst().ifPresent(f -> headers.put(KafkaHeaders.KEY, data.get(f.pos())));

        Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .ifPresent(user -> headers.put(AUTHENTICATED_USER, user));

        var message = MessageBuilder.createMessage(data, new MessageHeaders(headers));
        var channelId = channelId();
        log.trace(SENDING_MESSAGE_TO_CHANNEL_FORMAT, channelId);

        return doSend(message, channelId);
    }

    /**
     * Отправить сообщение с заголовками.
     *
     * @param data данные.
     * @param <T>  тип данных.
     * @return данные о задаче по отправке.
     */
    public <T extends Message<?>> Future<Void> send(List<T> data) {
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
    public <T extends Message<?>> Future<Void> send(List<T> data, List<Map<String, Object>> additionalHeaders) {
        var sourceHeaders = new ArrayList<>(additionalHeaders);
        var headers = new HashMap<String, Object>();
        for (var i = 0; i < data.size(); i++) {
            sourceHeaders.get(i).put(KafkaHeaders.KEY, data.get(i).getId());
        }
        headers.put(KafkaHeaders.BATCH_CONVERTED_HEADERS, sourceHeaders);

        Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .ifPresent(user -> headers.put(AUTHENTICATED_USER, user));

        var message = MessageBuilder.createMessage(data, new MessageHeaders(headers));
        var channelId = channelId();

        return doSend(message, channelId);
    }

    private Future<Void> doSend(org.springframework.messaging.Message<?> message, String channelId) {
        return executor.submit(() -> {
            log.trace(SENDING_MESSAGE_TO_CHANNEL_FORMAT, channelId);
            bridge.send(channelId, message);
            log.debug(SENT_MESSAGE_TO_CHANNEL_FORMAT, channelId);
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
}
