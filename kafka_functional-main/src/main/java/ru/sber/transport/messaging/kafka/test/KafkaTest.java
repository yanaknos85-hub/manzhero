package ru.sber.transport.messaging.kafka.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.messaging.Message;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Тестирование работы с каналами брокера.
 */
@SuppressWarnings({"SameParameterValue", "unchecked", "unused"})
@SpringBootTest
@Slf4j
@Import(TestChannelBinderConfiguration.class)
public abstract class KafkaTest {

    private static final String DESTINATION_SUFFIX = ".destination";

    /**
     * Название заголовка для указания поля с идентификатором сообщения.
     */
    protected static final String ID_FIELD_HEADER = "idField";

    private final Set<String> destinations = new HashSet<>();

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private BeanFactory beanFactory;

    @AfterEach
    void tearDownAll() {
        destinations.forEach(dest -> {
            //noinspection StatementWithEmptyBody
            while (outputDestination.receive(1_000, dest) != null) {
                // NOSONAR clearing queue without clearing channel.
            }
        });
        destinations.clear();
    }

    /**
     * Опубликовать сообщение в очереди.
     *
     * @param topic топик.
     * @param data  данные.
     * @param <V>   тип данных.
     */
    @SneakyThrows
    protected <V extends Message<?>> void produceMessage(String topic, V data) {
        produceMessage(topic, data, Map.of());
    }

    /**
     * Опубликовать сообщение в очереди.
     *
     * @param topic топик.
     * @param data  данные.
     * @param <V>   тип данных.
     */
    @SneakyThrows
    protected <V extends SpecificRecord> void produceMessage(String topic, V data) {
        produceMessage(topic, data, Map.of());
    }

    /**
     * Опубликовать сообщение в очереди.
     *
     * @param topic   топик.
     * @param data    данные.
     * @param headers заголовки, прикладываемые к сообщению.
     * @param <V>     тип данных.
     */
    @SneakyThrows
    protected <V extends Message<?>> void produceMessage(String topic, V data, @NonNull Map<String, Object> headers) {
        var builder = MessageBuilder.withPayload(data);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            var userName = authentication.getName();
            builder.setHeader(OutputBridge.AUTHENTICATED_USER, userName);
        }
        builder.setHeader(KafkaHeaders.KEY, data.getId());
        for (var header : headers.entrySet()) {
            builder.setHeader(header.getKey(), header.getValue());
        }
        produceMessage(topic, builder.build());
    }

    /**
     * Опубликовать сообщение в очереди.
     *
     * @param topic   топик.
     * @param data    данные.
     * @param headers заголовки, прикладываемые к сообщению.
     * @param <V>     тип данных.
     */
    @SneakyThrows
    protected <V extends SpecificRecord> void produceMessage(String topic, V data, @NonNull Map<String, Object> headers) {
        var effectiveHeaders = new HashMap<>(headers);
        var builder = MessageBuilder.withPayload(data);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            var userName = authentication.getName();
            builder.setHeader(OutputBridge.AUTHENTICATED_USER, userName);
        }
        data.getSchema().getFields()
                .stream()
                .filter(field -> field.aliases().contains("headerId"))
                .findFirst().ifPresent(f -> effectiveHeaders.put(KafkaHeaders.RECEIVED_KEY, data.get(f.pos())));
        for (var header : effectiveHeaders.entrySet()) {
            builder.setHeader(header.getKey(), header.getValue());
        }
        produceMessage(topic, builder.build());
    }

    /**
     * Отправить список сообщений с заголовками.
     *
     * @param topic    топик для отправки сообщений.
     * @param messages сообщение со списком объектов. В реальном использовании будет преобразовано в несколько сообщений.
     * @param headers  заголовки. Содержимое будет распространяться на все сообщения списка.
     * @param idField  название поля с идентификатором.
     * @param <V> тип объекта сообщения.
     * @param <C> тип коллекции объектов сообщения.
     */
    protected <V, C extends List<V>> void produceMessages(String topic, org.springframework.messaging.Message<C> messages, Map<String, Object> headers, String idField) {
        var headersList = new ArrayList<Map<String, Object>>();
        messages.getPayload().forEach(p -> {
            var item = new HashMap<>(headers);
            item.put(ID_FIELD_HEADER, idField);
            headersList.add(item);
        });
        produceMessages(topic, ReflectionUtils.cast(messages), headersList);
    }

    /**
     * Отправить список сообщений с заголовками.
     *
     * @param topic       топик для отправки сообщений.
     * @param messages    сообщение со списком объектов. В реальном использовании будет преобразовано в несколько
     *                   сообщений.
     * @param headersList заголовки. Количество объектов в списке заголовков должно совпадать с количеством сообщений.
     *                   Для указания поля-идентификатора необходимо добавить заголовок
     *                   idField ({@link KafkaTest#ID_FIELD_HEADER}).
     * @param <V> тип объекта сообщения.
     * @param <C> тип коллекции объектов сообщения.
     */
    @SneakyThrows({ClassNotFoundException.class})
    protected <V, C extends List<V>> void produceMessages(String topic, org.springframework.messaging.Message<C> messages, List<Map<String, Object>> headersList) {
        var payloadsList = messages.getPayload();
        assert payloadsList.size() == headersList.size() : "Payload size must be the same as headers size: one headers for one payload";
        var resultHeaders = new ArrayList<Map<String, Object>>();
        for (var i = 0; i < payloadsList.size(); i++) {
            var payloadItem = payloadsList.get(i);
            var headers = headersList.get(i);
            var headersItem = new HashMap<>(headers);
            var idField = headers.get(ID_FIELD_HEADER);
            if (payloadItem instanceof Map<?, ?> payloadMap) {
                var id = payloadMap.get(idField);
                if (id != null) {
                    headersItem.put(KafkaHeaders.RECEIVED_KEY, id);
                }
            }
            resultHeaders.add(headersItem);
        }
        var messageHeaders = new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS, resultHeaders));

        var message = MessageBuilder.createMessage(payloadsList, messageHeaders);

        var consumerDefinition = searchDefinition(topic);
        assert consumerDefinition != null : "Consumer for topic %s not found".formatted(topic);
        var consumer = beanFactory.getBean(consumerDefinition, Consumer.class);
        var consumerClass = consumer.getClass().getName();
        if (consumerClass.contains("$$Lambda")) {
            consumerClass = consumerClass.substring(0, consumerClass.indexOf("$"));
        }
        var consumerParent = Class.forName(consumerClass);
        var consumerMethod = Arrays.stream(consumerParent.getDeclaredMethods()).filter(m -> consumerDefinition.equals(m.getName())).findFirst().orElseThrow();

        var consumerType = ResolvableType.forMethodReturnType(consumerMethod);

        if (consumerType.hasGenerics()) {
            var generic = consumerType.getGeneric(0);
            if (org.springframework.messaging.Message.class.isAssignableFrom(Objects.requireNonNull(generic.getRawClass()))) {
                consumer.accept(message);
            } else {
                throw new UnsupportedOperationException("No consumer message %s".formatted(consumerType));
            }
        } else {
            throw new UnsupportedOperationException("There is generic consumer %s".formatted(consumerType));
        }
    }

    /**
     * Опубликовать сообщение в очереди.
     *
     * @param topic   топик.
     * @param message сырое сообщение.
     */
    @SneakyThrows({ClassNotFoundException.class})
    protected void produceMessage(String topic, org.springframework.messaging.Message<?> message) {
        var consumerDefinition = searchDefinition(topic);
        assert consumerDefinition != null : "Consumer for topic %s not found".formatted(topic);
        var consumer = beanFactory.getBean(consumerDefinition, Consumer.class);
        var consumerClass = consumer.getClass().getName();
        if (consumerClass.contains("$$Lambda")) {
            consumerClass = consumerClass.substring(0, consumerClass.indexOf("$"));
        }
        var consumerParent = Class.forName(consumerClass);
        var consumerMethod = Arrays.stream(consumerParent.getDeclaredMethods()).filter(m -> consumerDefinition.equals(m.getName())).findFirst().orElseThrow();

        var consumerType = ResolvableType.forMethodReturnType(consumerMethod);

        if (consumerType.hasGenerics()) {
            var generic = consumerType.getGeneric(0);
            if (org.springframework.messaging.Message.class.isAssignableFrom(Objects.requireNonNull(generic.getRawClass()))) {
                var messageType = generic.getGeneric(0);
                if (Collection.class.isAssignableFrom(Objects.requireNonNull(messageType.getRawClass()))) {
                    var payload = message.getPayload();
                    var headers = message.getHeaders();
                    var collectionMessage = MessageBuilder.createMessage(payload, headers);
                    consumer.accept(collectionMessage);
                } else {
                    consumer.accept(message);
                }
            } else {
                throw new UnsupportedOperationException("No consumer message %s".formatted(consumerType));
            }
        } else {
            throw new UnsupportedOperationException("There is generic consumer %s".formatted(consumerType));
        }
    }

    private String searchDefinition(String topic) {
        for (var source : environment.getPropertySources()) {
            var sourceMap = source.getSource();
            if (sourceMap instanceof Map) {
                for (var item : ((Map<?, ?>) sourceMap).entrySet()) {
                    var rawValue = item.getValue();
                    if (rawValue instanceof OriginTrackedValue trackedValue && topic.equals(trackedValue.getValue())) {
                        var key = String.valueOf(item.getKey());
                        return key.replace("spring.cloud.stream.bindings.", "").replace("-in-0.destination", "");
                    }
                }
            }
        }
        return null;
    }

    /**
     * Получить последнее сообщение из топика.
     *
     * @param topic      топик.
     * @param valueClass класс значения.
     * @param <V>        тип значения.
     * @return значение.
     */
    @SneakyThrows
    protected <V extends Message<?>> V consumeMessage(String topic, Class<V> valueClass) {
        return consumeMessage(topic, valueClass, headers -> true);
    }

    /**
     * Получить последнее сообщение из топика.
     *
     * @param topic      топик.
     * @param valueClass класс значения.
     * @param <V>        тип значения.
     * @return значение.
     */
    @SneakyThrows
    protected <V extends SpecificRecord> V consumeMessageAvro(String topic, Class<V> valueClass) {
        return consumeMessageAvro(topic, valueClass, headers -> true);
    }

    /**
     * Получить последнее сообщение из топика по заголовку.
     *
     * @param topic            топик.
     * @param valueClass       класс значения.
     * @param headersPredicate условие получения сообщения, связанное с заголовком.
     * @param <V>              тип значения.
     * @return значение.
     */
    @SneakyThrows
    protected <V extends Message<?>> V consumeMessage(String topic, Class<V> valueClass, @NonNull Predicate<MessageHeaders> headersPredicate) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        V result = null;
        org.springframework.messaging.Message<?> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var headers = message.getHeaders();
                if (headersPredicate.test(headers)) {
                    var payload = message.getPayload();
                    if (payload instanceof byte[] bytes) {
                        result = mapper.readValue(bytes, valueClass);
                    } else {
                        result = ReflectionUtils.cast(payload);
                    }
                }
            }
        } while (message != null);
        return result;
    }

    /**
     * Получить последнее сообщение из топика по заголовку.
     *
     * @param topic            топик.
     * @param valueClass       класс значения.
     * @param headersPredicate условие получения сообщения, связанное с заголовком.
     * @param <V>              тип значения.
     * @return значение.
     */
    @SneakyThrows
    protected <V extends SpecificRecord> V consumeMessageAvro(String topic, Class<V> valueClass, @NonNull Predicate<MessageHeaders> headersPredicate) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        V result = null;
        org.springframework.messaging.Message<?> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var headers = message.getHeaders();
                if (headersPredicate.test(headers)) {
                    result = ReflectionUtils.cast(message.getPayload());
                }
            }
        } while (message != null);
        return result;
    }

    /**
     * Получить все сообщения из топика с разбиением по ключам.
     *
     * @param topic      топик.
     * @param keyClass   класс ключа.
     * @param valueClass класс значения.
     * @param <K>        тип ключа.
     * @param <V>        тип значения.
     * @return список сообщений.
     */
    @SneakyThrows
    protected <K, V extends Message<?>> Map<K, List<V>> consumeMessages(String topic, Class<K> keyClass, Class<V> valueClass) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        var results = new HashMap<K, List<V>>();
        org.springframework.messaging.Message<?> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var key = ReflectionUtils.cast(message.getHeaders().get(KafkaHeaders.KEY), keyClass);
                var payload = message.getPayload();
                V value;
                if (payload instanceof byte[] bytes) {
                    value = mapper.readValue(bytes, valueClass);
                } else {
                    value = ReflectionUtils.cast(payload);
                }
                var list = results.getOrDefault(key, new ArrayList<>());
                list.add(value);
                results.put(key, list);
            }
        } while (message != null);
        return results;
    }

    /**
     * Получить все сообщения из топика с разбиением по ключам.
     *
     * @param topic      топик.
     * @param keyClass   класс ключа.
     * @param valueClass класс значения.
     * @param <K>        тип ключа.
     * @param <V>        тип значения.
     * @return список сообщений.
     */
    @SneakyThrows
    protected <K, V extends SpecificRecord> Map<K, List<V>> consumeMessagesAvro(String topic, Class<K> keyClass, Class<V> valueClass) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        var results = new HashMap<K, List<V>>();
        org.springframework.messaging.Message<byte[]> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var key = ReflectionUtils.cast(message.getHeaders().get(KafkaHeaders.KEY), keyClass);
                var value = ReflectionUtils.cast(message.getPayload(), valueClass);
                var list = results.getOrDefault(key, new ArrayList<>());
                list.add(value);
                results.put(key, list);
            }
        } while (message != null);
        return results;
    }

    /**
     * Получить все сообщения из топика.
     *
     * @param topic      топик.
     * @param valueClass класс значения.
     * @param <V>        тип значения.
     * @return список сообщений.
     */
    @SneakyThrows
    protected <V extends Message<?>> List<V> consumeMessages(String topic, Class<V> valueClass) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        var results = new ArrayList<V>();
        org.springframework.messaging.Message<?> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var payload = message.getPayload();
                V value;
                if (payload instanceof byte[] bytes) {
                    value = mapper.readValue(bytes, valueClass);
                } else {
                    value = ReflectionUtils.cast(payload);
                }
                results.add(value);
            }
        } while (message != null);
        return results;
    }

    /**
     * Получить все сообщения из топика.
     *
     * @param topic      топик.
     * @param valueClass класс значения.
     * @param <V>        тип значения.
     * @return список сообщений.
     */
    @SneakyThrows
    protected <V extends SpecificRecord> List<V> consumeMessagesAvro(String topic, Class<V> valueClass) {
        var destination = topic + DESTINATION_SUFFIX;
        destinations.add(destination);
        var results = new ArrayList<V>();
        org.springframework.messaging.Message<byte[]> message;
        do {
            message = outputDestination.receive(1_000, destination);
            if (message != null) {
                var value = ReflectionUtils.cast(message.getPayload(), valueClass);
                results.add(value);
            }
        } while (message != null);
        return results;
    }

}
