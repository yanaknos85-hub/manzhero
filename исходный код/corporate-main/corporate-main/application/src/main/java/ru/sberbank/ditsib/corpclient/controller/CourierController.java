package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping({"/self/courier", "/self/courier/"})
public interface CourierController {

    /**
     * Добавление роли 'Курьер' текущему пользователю
     *
     * @param authentication токен авторизации
     */
    @PutMapping
    @Operation(summary = "Добавление роли 'Курьер' текущему пользователю",
            description = "Добавление роли 'Курьер' текущему пользователю")
    void addCourierRole(@Parameter(hidden = true) JwtAuthenticationToken authentication);

    /**
     * Удаление роли 'Курьер' у текущего пользователя
     *
     * @param authentication токен авторизации
     */
    @DeleteMapping
    @Operation(summary = "Удаление роли 'Курьер' у текущего пользователя",
            description = "Удаление роли 'Курьер' у текущего пользователя")
    void deleteCourierRole(@Parameter(hidden = true) JwtAuthenticationToken authentication);

}
