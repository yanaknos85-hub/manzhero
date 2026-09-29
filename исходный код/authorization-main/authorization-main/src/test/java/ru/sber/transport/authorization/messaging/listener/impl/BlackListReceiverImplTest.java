package ru.sber.transport.authorization.messaging.listener.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.authorization.messaging.listener.BlackListReceiver;
import ru.sber.transport.authorization.service.BlackListService;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@DisplayName("Проверка получателя данных ЧС")
class BlackListReceiverImplTest {

    private final BlackListService blackListService = mock(BlackListService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BlackListReceiver blackListReceiver = new BlackListReceiverImpl(blackListService, objectMapper);

    @Test
    @DisplayName("Проверка получения")
    void test_newData() throws IOException {
        var token = """
            {
                "token": "TokenData"
            }""".getBytes(StandardCharsets.UTF_8);

        blackListReceiver.handle(token);

        verify(blackListService).add("TokenData");
    }

}