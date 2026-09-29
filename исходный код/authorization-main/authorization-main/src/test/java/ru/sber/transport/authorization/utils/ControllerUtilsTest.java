package ru.sber.transport.authorization.utils;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("lib_authorization")
@DisplayName("Проверка функционала утилит контроллера")
class ControllerUtilsTest {

    @AfterEach
    void afterEach() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @DisplayName("Проверка получения пользователя из токена. Нет авторизованного пользователя")
    void test_get_user_noUser() {
        assertThat(ControllerUtils.currentUser()).isNull();
    }

    @Test
    @DisplayName("Проверка получения пользователя из токена. Нет авторизованного пользователя")
    void test_get_user() {
        final var id = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(id.toString()).build()));

        assertThat(ControllerUtils.currentUser()).isEqualTo(id);
    }

    @Test
    @DisplayName("Проверка получения прав пользователя. Нет авторизованного пользователя")
    void test_get_rights_noUser() {
        assertThat(ControllerUtils.isDataMaster()).isFalse();
    }

    @Test
    @DisplayName("Проверка получения прав пользователя")
    void test_get_rights() {
        final var id = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(id.toString()).claim("data_master", true).build()));

        assertThat(ControllerUtils.isDataMaster()).isTrue();
    }

    @Test
    @DisplayName("Проверка получения прав пользователя. Нет прав")
    void test_get_rights_no() {
        final var id = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none").jti(id.toString()).claim("data_master", false).build()));

        assertThat(ControllerUtils.isDataMaster()).isFalse();
    }

}