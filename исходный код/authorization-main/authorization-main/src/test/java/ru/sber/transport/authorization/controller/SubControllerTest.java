package ru.sber.transport.authorization.controller;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.AuthorizationConfiguration;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@ContextConfiguration(classes = AuthorizationConfiguration.class)
@DisplayName("Проверка авторизации")
@WebMvcTest
@TestPropertySource(properties = "classpath:/application.yml")
class SubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("Дополнительная проверка. Подзапрос")
    @Test
    void test_subRequest() throws Exception {
        mockMvc.perform(put("/sub/login/token/"))
            .andExpect(status().isOk());
    }

    @DisplayName("Дополнительная проверка")
    @Test
    void test_request() throws Exception {
        mockMvc.perform(delete("/sub"))
            .andExpect(status().isOk());
    }

    @DisplayName("Дополнительная проверка. Patch")
    @Test
    void test_patch() throws Exception {
        mockMvc.perform(patch("/sub"))
            .andExpect(status().isOk());
    }

    @DisplayName("Дополнительная проверка. Get")
    @Test
    void test_get() throws Exception {
        mockMvc.perform(get("/sub"))
            .andExpect(status().isOk());
    }

}