package ru.sberbank.ditsib.transport.sudir;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.fail;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@EmbeddedPostgres
@DisplayName("Проверка запуска")
class SudirApplicationTest {
    
    @DisplayName("Запуск")
    @Test
    void test() {
        try {
            SudirApplication.main("--spring.profiles.active=test");
        } catch (Exception e) {
            fail("Startup failed", e);
        }
    }
    
}