UPDATE configs.properties
SET value='ru.sber.transport.messaging.kafka.serialize.KafkaAvroSerialize'
WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.value.serializer' AND value='io.confluent.kafka.serializers.KafkaAvroSerializer' AND application='application' AND profile='kafka-avro' AND "label"='master';
UPDATE configs.properties
SET value='io.confluent.kafka.serializers.KafkaAvroDeserializer'
WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.value.deserializer' AND value='ru.sber.transport.messaging.kafka.serialize.KafkaAvroDeserialize' AND application='application' AND profile='kafka-avro' AND "label"='master';

