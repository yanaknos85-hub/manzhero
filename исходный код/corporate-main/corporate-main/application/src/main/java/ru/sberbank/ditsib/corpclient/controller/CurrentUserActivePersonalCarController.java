package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Контроллер ЛС по умолчанию
 */
@RequestMapping({"/self/cars", "/self/cars/"})
@Tag(name = "Активный личный транспорт", description = "Набор операций для работы с активным личным транспортом " +
        "текущего пользователя")
public interface CurrentUserActivePersonalCarController {

    @Operation(summary = "Сохранение",
            description = "Первичное сохранение или изменение личного автомобиля по умолчанию для вошедшего пользователя)")
    @PutMapping()
    void save(
            @NotNull @RequestParam UUID personalCarId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @Operation(summary = "Получение", description = "Получение активного ЛТ текущего пользователя")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    PersonalCarDTO get(
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

    @GetMapping(value = {"agreement/third-party", "agreement/third-party/"}, produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Добавление", description = "Получение формы согласия 3-х лиц")
    ResponseEntity<InputStreamResource> getAgreement();
}
