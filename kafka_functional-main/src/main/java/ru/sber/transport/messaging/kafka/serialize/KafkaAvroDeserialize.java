package ru.sber.transport.messaging.kafka.serialize;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Сериализатор сообщений кафки
 */
@Slf4j
public class KafkaAvroDeserialize extends KafkaAvroDeserializer {

    private final CompatibleUpdater compatibleUpdater = new CompatibleUpdater();

    @SneakyThrows({IOException.class, RestClientException.class})
    @Override
    protected String getSubjectName(String topic, boolean isKey, Object value, ParsedSchema schema) {
        final var name = super.getSubjectName(topic, isKey, value, schema);
        compatibleUpdater.update(schemaRegistry, topic, name);
        return name;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs, isKey);
        compatibleUpdater.configure(configs);
    }

}
