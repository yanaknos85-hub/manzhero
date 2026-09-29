package ru.sberbank.ditsib.transport.limits;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unused")
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка запуска")
@Disabled("Требуется доработка")
class LimitsApplicationTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("Запуск")
    void test_main() {
        try {
            LimitsApplication.main("--spring.profiles.active=test", "--spring.cloud.grpc.discovery.server=localhost:6565");
        } catch(Exception e) {
            fail("Startup failed", e);
        }
    }
}