package ru.sber.transport.authorization.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@DisplayName("Проверка сервиса ЧС")
class BlackListServiceImplTest {

    @Test
    @DisplayName("Проверка ЖЦ ЧС")
    void test_blackList() {
        var service = new BlackListServiceImpl();

        service.add("token1");
        service.add("token2");
        service.add("token3");
        service.add(null);

        assertTrue(service.check("token2"));

        service.cleanupTokens();
        service.cleanupTokens();

        assertFalse(service.check("token2"));
    }

}