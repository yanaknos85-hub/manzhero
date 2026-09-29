package ru.sberbank.ditsib.transport.limits.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.transport.limits.dto.BonusDTO;

import java.util.UUID;

@RequestMapping(value = "/bonus")
@Tag(name = "Бонусный счёт", description = "Набор операций для работы с бонусным счётом")
public interface BonusController {
    
    @GetMapping("/{ownerId}")
    @Operation(summary = "Получить данные о состояние бонусного счёта пользователя",
               description = "Получить данные о состояние бонусного счёта пользователя")
    BonusDTO get(@PathVariable UUID ownerId, @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
}
