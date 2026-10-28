package ru.sber.transport.jwks;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_jwks")
@DisplayName("Проверка запуска")
class ApplicationTest {

    @Test
    @DisplayName("Запуск")
    void test() {
        try {
            Application.main();
        } catch (Exception e) {
            fail(e);
        }
    }

}