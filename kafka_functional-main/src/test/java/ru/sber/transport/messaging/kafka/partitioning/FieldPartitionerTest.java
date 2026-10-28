package ru.sber.transport.messaging.kafka.partitioning;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.avro.messages.TestMessage;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_kafka_functional")
@DisplayName("Проверка партиционирования сообщений по полям")
class FieldPartitionerTest {

    private final FieldPartitioner partitioner = new FieldPartitioner();

    @Test
    @DisplayName("Проверка получения партиции")
    void test_partition() {
        var cluster = mock(Cluster.class);

        when(cluster.availablePartitionsForTopic("test")).thenReturn(IntStream.range(0, 2).mapToObj(i -> Instancio.create(PartitionInfo.class)).toList());
        when(cluster.availablePartitionsForTopic("test-avro")).thenReturn(IntStream.range(0, 3).mapToObj(i -> Instancio.create(PartitionInfo.class)).toList());

        var config = Map.of(
                "topic.test.partition", "id",
                "topic.test-avro.partition", "id"
        );

        partitioner.configure(config);

        var test = new TestData(1L);
        var map = Map.of("id", "2");
        var map2 = Map.of("id", "test key");
        var map3 = Map.of();
        var testAvro = TestMessage.newBuilder().setId(UUID.randomUUID()).setText("Text").build();

        assertThat(partitioner.partition("test", "key", null, test, null, cluster)).isEqualTo(1 % 2);
        assertThat(partitioner.partition("test", "key", null, map, null, cluster)).isZero();
        assertThat(partitioner.partition("test", "key", null, map2, null, cluster)).isEqualTo(Math.abs("test key".hashCode() % 2));
        assertThat(partitioner.partition("test", "key", null, map3, null, cluster)).isEqualTo("key".hashCode() % 2);
        assertThat(partitioner.partition("test-avro", "key", null, testAvro, null, cluster)).isEqualTo(Math.abs(testAvro.getId().hashCode() % 3));
        assertThat(partitioner.partition("test-avro", "key", null, map3, null, cluster)).isEqualTo(Math.abs("key".hashCode() % 3));
    }

    @RequiredArgsConstructor
    private static class TestData {

        @SuppressWarnings("unused")
        final Long id;
    }

}