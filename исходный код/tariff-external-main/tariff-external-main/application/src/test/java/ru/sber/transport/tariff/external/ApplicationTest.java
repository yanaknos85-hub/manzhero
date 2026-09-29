package ru.sber.transport.tariff.external;

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
@Feature("app_platform_tariff_external")
@DisplayName("Проверка запуска сервиса")
class ApplicationTest {

    @Test
    @DisplayName("Проверка запуска")
    void test() {
        try {
            Application.main("--spring.profiles.include=test");
        } catch (Exception e) {
            fail(e);
        }
    }

}