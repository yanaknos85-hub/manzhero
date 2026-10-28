package ru.sber.transport.messaging.kafka.serialize;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_kafka_functional")
@DisplayName("Проверка сериализации сообщений")
class KafkaAvroSerializeTest {

    private final SchemaRegistryClient schemaRegistry = mock(SchemaRegistryClient.class);

    private final TestKafkaAvroSerialize serialize = new TestKafkaAvroSerialize();

    @Test
    @DisplayName("Проверка получения имени субъекта")
    void test_getSubjectName() throws RestClientException, IOException {
        var configs = Map.of(
                "schema.registry.url", "http://localhost:8081",
                "schema.registry.topic.compatible", "BACKWARD",
                "schema.registry.global.compatible", "FULL"
        );

        when(schemaRegistry.updateCompatibility("BACKWARD", "topic")).thenReturn("BACKWARD");
        when(schemaRegistry.updateCompatibility("FULL", "global")).thenReturn("FULL");
        when(schemaRegistry.getCompatibility("topic-value")).thenReturn("NON-BACKWARD");
        when(schemaRegistry.getCompatibility("topic-second-value")).thenReturn("NON-FULL");
        when(schemaRegistry.getCompatibility("global-value")).thenReturn("NON-FULL");

        serialize.configure(configs, false);

        serialize.setSchemaRegistry(schemaRegistry);

        serialize.getSubjectName("topic", false, null, null);
        serialize.getSubjectName("topic-second", false, null, null);
        serialize.getSubjectName("global", false, null, null);

        verify(schemaRegistry).updateCompatibility("topic-value", "BACKWARD");
        verify(schemaRegistry).updateCompatibility("topic-second-value", "FULL");
        verify(schemaRegistry).updateCompatibility("global-value", "FULL");
    }

    private static class TestKafkaAvroSerialize extends KafkaAvroSerialize {

        public void setSchemaRegistry(SchemaRegistryClient schemaRegistry) {
            this.schemaRegistry = schemaRegistry;
        }

        @Override
        public String getSubjectName(String topic, boolean isKey, Object value, ParsedSchema schema) {
            return super.getSubjectName(topic, isKey, value, schema);
        }

    }

}