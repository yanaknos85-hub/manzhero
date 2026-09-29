package ru.sber.transport.tariff.external;


import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.tariff.external.providers.tariff.config.ExchangeProperties;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_tariff_external")
@DisplayName("Проверка контекста")
@SpringBootTest
@ActiveProfiles("test")
class ContextTest {

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Проверка окружения")
    void test_environment() {
        assertThat(env.getProperty("exchange.request.api-key")).isEqualTo("test-api-key");
        assertThat(env.getProperty("exchange.request.client-id")).isEqualTo("test-client-id");
        assertThat(env.getProperty("exchange.request.base-url")).isEqualTo("test-base-url");
    }

    @Test
    @DisplayName("Проверка контекста")
    void test_context() {
        final var properties = context.getBean(ExchangeProperties.class);

        assertThat(properties.getRequest().apiKey()).isEqualTo("test-api-key");
        assertThat(properties.getRequest().clientId()).isEqualTo("test-client-id");
        assertThat(properties.getRequest().baseUrl()).isEqualTo("test-base-url");
    }

}
