INSERT INTO configs.properties ("key",value,application,profile,"label")
	VALUES ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.records.policy.enabled','false','application','kafka-avro','master');
INSERT INTO configs.properties ("key",value,application,profile,"label")
	VALUES ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.records.policy.enabled','false','application','kafka-avro','master');
delete from config.properties where "key" = 'spring.profiles.include';