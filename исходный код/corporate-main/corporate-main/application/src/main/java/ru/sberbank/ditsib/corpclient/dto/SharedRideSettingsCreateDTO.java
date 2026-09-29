package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Информация о новых настройках совместных поездок. Не содержит ID и ID элементов настроек
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(
        title = "Информация о новых настройках совместных поездок",
        description = "Информация о новых настройках совместных поездок"
)
public class SharedRideSettingsCreateDTO extends SharedRideSettingsDTO {

    /**
     * Словарь "тип настройки - новые элементы настройки"
     */
    @Schema(description = "Словарь \"тип настройки - новые элементы настройки\"")
    @NotNull
    private Map<@NotBlank String, @Valid SharedRideSettingsItemCreateDTO> settings;
}
