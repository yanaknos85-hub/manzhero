package ru.sber.transport.messaging.kafka.partitioning;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Механизм партиционирования сообщений по полю
 */
class FieldPartitioner implements Partitioner {

    private static final String TOPIC_PARTITION_PREFIX = "topic.";

    private static final String TOPIC_PARTITION_SUFFIX = ".partition";

    private final Map<String, String> topicFields = new HashMap<>();

    @Override
    public void configure(Map<String, ?> configs) {
        for (var config : configs.entrySet()) {
            var key = config.getKey();
            var value = config.getValue();
            if (key.startsWith(TOPIC_PARTITION_PREFIX) && key.endsWith(TOPIC_PARTITION_SUFFIX)) {
                topicFields.put(key.replace(TOPIC_PARTITION_PREFIX, "").replace(TOPIC_PARTITION_SUFFIX, ""), value.toString());
            }
        }
    }

    @Override
    public void close() {
        // Нет дополнительных действий на закрытие партиционера
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        final var partitions = cluster.availablePartitionsForTopic(topic);
        var partitionIndex = Math.abs(key.hashCode() % partitions.size());
        final var field = topicFields.get(topic);
        if (field != null) {
            final Object data;
            if (value instanceof SpecificRecord specificRecord) {
                data = getValue(specificRecord, field);
            } else if (value instanceof Map<?, ?> map) {
                data = map.get(field);
            } else {
                data = getValue(value, field);
            }
            if (data != null) {
                if (data instanceof Number number) {
                    partitionIndex = number.intValue() % partitions.size();
                } else if (data instanceof String string && string.matches("^\\d+$")) {
                    partitionIndex = (int) Long.parseLong(string) % partitions.size();
                } else {
                    partitionIndex = Math.abs(data.hashCode() % partitions.size());
                }
            }
        }
        return partitionIndex;
    }

    private Object getValue(SpecificRecord specificRecord, String field) {
        final var partitionField = specificRecord.getSchema().getField(field);
        if (partitionField != null) {
            return specificRecord.get(partitionField.pos());
        }
        return null;
    }

    private Object getValue(Object data, String field) {
        final var reflectedField = ReflectionUtils.getField(data.getClass(), field);
        try {
            return reflectedField.get(data);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
    }

}