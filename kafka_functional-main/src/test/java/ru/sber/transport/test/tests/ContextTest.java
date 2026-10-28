package ru.sber.transport.test.tests;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.test.DoNotStartThis;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@IsolatedTest
@Feature("lib_kafka_functional")
@SpringBootTest(classes = DoNotStartThis.class)
public class ContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @DisplayName("Проверка контекста")
    @Test
    void test_context() {
        assertThat(applicationContext.getBeanNamesForType(StreamBridge.class)).hasSize(1);
        assertThat(applicationContext.getBeanNamesForType(OutputBridge.class)).hasSize(1);
        assertThat(applicationContext.getBeanNamesForType(OutputBridge.class)[0]).isEqualTo("someTestOut");
    }
}
