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
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@WebMvcTest
@ContextConfiguration(classes = AuthorizationConfiguration.class)
@DisplayName("Проверка сторонней авторизации")
@TestPropertySource(properties = "classpath:/application.yml")
@NoAuthorize("/foreign/**")
class ForeignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Проверка авторизации")
    void test_foreign() throws Exception {
        mockMvc.perform(get("/foreign/controller/"))
            .andExpect(status().isOk());
    }

}