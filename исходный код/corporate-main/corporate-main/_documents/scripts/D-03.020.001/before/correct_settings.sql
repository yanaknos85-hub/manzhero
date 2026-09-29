DELETE FROM configs.properties
	WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.records.policy.name' AND value='https-policy' AND application='application' AND profile='kafka-avro' AND "label"='master';
DELETE FROM configs.properties
	WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.records.policy.enabled' AND value='true' AND application='application' AND profile='kafka-avro' AND "label"='master';
DELETE FROM configs.properties
	WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.records.policy.name' AND value='https-policy' AND application='application' AND profile='kafka-avro' AND "label"='master';
DELETE FROM configs.properties
	WHERE "key"='spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.records.policy.enabled' AND value='true' AND application='application' AND profile='kafka-avro' AND "label"='master';

INSERT INTO configs.properties ("key",value,application,profile,"label")
	VALUES ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','common','master');
INSERT INTO configs.properties ("key",value,application,profile,"label")
	VALUES ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','common','master');
INSERT INTO configs.properties ("key",value,application,profile,"label")
	VALUES ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','common','master');
