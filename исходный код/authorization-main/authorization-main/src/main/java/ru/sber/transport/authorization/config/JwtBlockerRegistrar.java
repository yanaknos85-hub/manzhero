package ru.sber.transport.authorization.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.sber.transport.authorization.messaging.listener.BlackListReceiver;
import ru.sber.transport.authorization.messaging.listener.impl.BlackListReceiverImpl;
import ru.sber.transport.authorization.service.BlackListService;

import java.util.HashMap;
import java.util.Map;

/**
 * Регистрация блокировщика токенов.
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.stream.default-binder", havingValue = "kafka")
public class JwtBlockerRegistrar {

    /**
     * Конфигурация получателя.
     *
     * @param service сервис обработки токенов ЧС.
     * @param mapper маппер для чтения данных.
     * @return ЧС токенов.
     */
    @Bean
    public BlackListReceiver configureReceiver(
            BlackListService service, ObjectMapper mapper
                                       ) {
        return new BlackListReceiverImpl(service, mapper);
    }

    /**
     * Формирование фабрики слушателей данных.
     *
     * @param consumerFactory фабрика получателей.
     * @return фабрика слушателей данных.
     */
    @Bean(value = "blackListKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<Integer, Map<String, String>> kafkaListenerContainerFactory(
            @Qualifier(("blackListKafkaConsumerFactory")) ConsumerFactory<Integer, Map<String, String>> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Integer, Map<String, String>>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * Формирование фабрики получателя данных черного списка токенов.
     *
     * @param environment окружение.
     * @return фабрика получателя.
     */
    @Bean(value = "blackListKafkaConsumerFactory")
    public ConsumerFactory<Integer, Map<String, String>> consumerFactory(Environment environment) {
        return new DefaultKafkaConsumerFactory<>(consumerProps(environment));
    }
    
    private Map<String, Object> consumerProps(Environment environment) {
        var props = new HashMap<String, Object>();
        var brokersString = getSingle(environment);
        if (brokersString.isBlank()) {
            brokersString = getDefault(environment);
        }
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokersString);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.application.name"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.keyDeserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.valueDeserializer"));
        return props;
    }

    private String getDefault(Environment environment) {
        var binderPrefix = "spring.cloud.stream";
        var defaultBinder = environment.getProperty("%s.default-binder".formatted(binderPrefix));
        return getBrokers(environment, "%s.binders.%s.environment.spring.cloud.stream.kafka.binder.brokers".formatted(binderPrefix, defaultBinder));
    }

    private String getSingle(Environment environment) {
        return getBrokers(environment, "spring.cloud.stream.kafka.binder.brokers");
    }

    private String getBrokers(Environment environment, String brokersPrefix) {
        var brokersString = new StringBuilder();
        var brokerIndex = 0;
        while(true) {
            var currentBroker = environment.getProperty(brokersPrefix + "[" + brokerIndex + "]");
            if (currentBroker == null) {
                break;
            }
            if (!brokersString.toString().isBlank()) {
                brokersString.append(",");
            }
            brokersString.append(currentBroker);
            brokerIndex++;
        }
        return brokersString.toString();
    }
}
