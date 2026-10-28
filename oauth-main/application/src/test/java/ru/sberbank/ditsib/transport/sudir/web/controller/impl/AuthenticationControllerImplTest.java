package ru.sberbank.ditsib.transport.sudir.web.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.sudir.business.model.Role;
import ru.sberbank.ditsib.transport.sudir.business.model.Token;
import ru.sberbank.ditsib.transport.sudir.business.exceptions.RefreshTokenExpired;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;

import java.security.Key;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = {JwtDecoder.class, Key.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Accounts accounts;

    @Test
    @DisplayName("Логин")
    void test_login() throws Exception, RefreshTokenExpired {
        var token = Instancio.create(Token.class);
        when(accounts.relogin(eq("token"), any(), any())).thenReturn(token);

        mockMvc.perform(post("/login").header(HttpHeaders.AUTHORIZATION, "Token token"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.token").value(token.getAccessToken()),
                jsonPath("$.refreshToken").value(token.getRefreshToken())
            );
    }

    @Test
    @DisplayName("Неверные данные")
    void test_login_wrongData() throws Exception, RefreshTokenExpired {
        var token = Instancio.create(Token.class);
        when(accounts.relogin(eq("token"), any(), any())).thenReturn(token);

        mockMvc.perform(post("/login").header(HttpHeaders.AUTHORIZATION, "Token token token"))
            .andExpectAll(
                status().isUnauthorized()
            );
    }

    @Test
    @DisplayName("Неверний тип входа")
    void test_login_wrongType() throws Exception, RefreshTokenExpired {
        var token = Instancio.create(Token.class);
        when(accounts.relogin(eq("token"), any(), any())).thenReturn(token);

        mockMvc.perform(post("/login").header(HttpHeaders.AUTHORIZATION, "Abc token"))
            .andExpectAll(
                status().isUnauthorized()
            );
    }

    @Test
    @DisplayName("Получение ролей")
    void test_getRoles() throws Exception {
        var userId = UUID.randomUUID();
        var roles = Instancio.ofSet(Role.class).create();

        when(accounts.getRoles(userId)).thenReturn(roles);

        mockMvc.perform(get("/roles/" + userId))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.length()").value(roles.size())
            );
    }

}