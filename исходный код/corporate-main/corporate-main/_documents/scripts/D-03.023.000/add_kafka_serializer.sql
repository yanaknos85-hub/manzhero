-- Auto-generated SQL script #202412241313
INSERT INTO configs.properties ("key", value, application, profile, "label")
VALUES ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer',
        'org.springframework.kafka.support.serializer.JsonDeserializer', 'application', 'kafka-avro', 'master');
INSERT INTO configs.properties ("key", value, application, profile, "label")
VALUES ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer',
        'org.springframework.kafka.support.serializer.JsonDeserializer', 'application', 'kafka', 'master');
INSERT INTO configs.properties ("key", value, application, profile, "label")
VALUES ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer',
        'org.springframework.kafka.support.serializer.JsonDeserializer', 'application', 'kafka-ssl', 'master');
INSERT INTO configs.properties ("key", value, application, profile, "label")
VALUES ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.producerProperties.key.serializer',
        'org.springframework.kafka.support.serializer.JsonSerializer', 'application', 'kafka', 'master');
INSERT INTO configs.properties ("key", value, application, profile, "label")
VALUES ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.producerProperties.key.serializer',
        'org.springframework.kafka.support.serializer.JsonSerializer', 'application', 'kafka-ssl', 'master');