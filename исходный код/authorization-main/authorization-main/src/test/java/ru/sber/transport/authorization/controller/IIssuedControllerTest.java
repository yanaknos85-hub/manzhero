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
@DisplayName("Проверка авторизации issued")
@WebMvcTest
@TestPropertySource(properties = "classpath:/application.yml")
class IIssuedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Проверка авторизации. GET")
    void test_get() throws Exception {
        mockMvc.perform(get("/issued/method"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка авторизации. POST")
    void test_post() throws Exception {
        mockMvc.perform(post("/issued/method"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка авторизации. PUT")
    void test_put() throws Exception {
        mockMvc.perform(put("/issued/method"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка авторизации. PATCH")
    void test_patch() throws Exception {
        mockMvc.perform(patch("/issued/method"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка авторизации. DELETE")
    void test_delete() throws Exception {
        mockMvc.perform(delete("/issued/method"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Проверка авторизации. Valued")
    void test_valued() throws Exception {
        mockMvc.perform(get("/issued/method/method/value/value"))
            .andExpect(status().isUnauthorized());
    }

}