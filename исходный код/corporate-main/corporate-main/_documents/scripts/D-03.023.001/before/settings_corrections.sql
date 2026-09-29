INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','kafka','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','kafka-ssl','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.requiredAcks','all','application','kafka-avro','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.producer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka-ssl','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.producer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka-ssl','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka-avro','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.replication.factor','#{"${spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers}".split('','').length()}','application','kafka-avro','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.auto-alter-topics','true','application','kafka','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.auto-alter-topics','true','application','kafka-ssl','master')
	 on conflict do nothing;
INSERT INTO configs.properties ("key",value,application,profile,"label") VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.auto-alter-topics','true','application','kafka-avro','master')
	 on conflict do nothing;
