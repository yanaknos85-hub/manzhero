package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Authorization
 */
public record Authorization(
        @Schema(name = "accessToken",
                description = "Токен доступа к ресурсам. Содержит информацию о вошедшем пользователе. Его необходимо отправлять заголовком Authorization: Bearer {accessToken} с каждым запросом")
        @JsonAlias({ "token", "accessToken" })
        String accessToken,
        @Schema(name = "refreshToken",
                description = "Токен повторного входа. Отправлять с заголовком Authorization: Token {refreshToken} при истечении срока жизни токена доступа")
        String refreshToken,
        @Schema(name = "transferPassword", description = "Транспортный пароль")
        @JsonProperty("transferPassword")
        Boolean isTransferPassword,
        @Schema(name = "accessExpiration", description = "Дата и время истечении срока жизни accessToken")
        LocalDateTime accessExpiration,
        @Schema(name = "refreshExpiration", description = "Дата и время истечении срока жизни refreshToken")
        LocalDateTime refreshExpiration
) {
}

