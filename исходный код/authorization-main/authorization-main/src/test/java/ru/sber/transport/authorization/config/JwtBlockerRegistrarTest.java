package ru.sber.transport.authorization.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.ConsumerFactory;
import ru.sber.transport.authorization.messaging.listener.impl.BlackListReceiverImpl;
import ru.sber.transport.authorization.service.BlackListService;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@DisplayName("Проверка регистрации блокировщика токенов")
class JwtBlockerRegistrarTest {

    @Test
    @DisplayName("Проверка формирование ресивера")
    void test_configureReceiver() {
        var registrar = new JwtBlockerRegistrar();

        var service = mock(BlackListService.class);
        var mapper = mock(ObjectMapper.class);

        var receiver = registrar.configureReceiver(service, mapper);

        assertThat(receiver)
                .isNotNull()
                .isInstanceOf(BlackListReceiverImpl.class);
    }

    @Test
    @DisplayName("Проверка формирования фабрики контейнеров консьюмеров")
    void test_configureKafkaListenerContainerFactory() {
        var registrar = new JwtBlockerRegistrar();

        ConsumerFactory<Integer, Map<String, String>> consumerFactory = mock(ConsumerFactory.class);

        var factory = registrar.kafkaListenerContainerFactory(consumerFactory);

        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isEqualTo(consumerFactory);
    }

    @Test
    @DisplayName("Проверка формирования фабрики консьюмеров")
    void test_configureConsumerFactory() {
        var registrar = new JwtBlockerRegistrar();

        var environment = mock(Environment.class);
        when(environment.getProperty(anyString())).thenAnswer(inv -> {
            var arg = inv.getArgument(0, String.class);
            if ("spring.cloud.stream.kafka.binder.brokers[2]".equals(arg)) {
                return null;
            }
            return arg;
        });

        var factory = registrar.consumerFactory(environment);

        assertThat(factory).isNotNull();
        assertThat(factory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "spring.cloud.stream.kafka.binder.brokers[0],spring.cloud.stream.kafka.binder.brokers[1]")
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "spring.application.name")
                .containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
                .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true)
                .containsEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "spring.kafka.consumer.keyDeserializer")
                .containsEntry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "spring.kafka.consumer.valueDeserializer")
                ;
    }

}