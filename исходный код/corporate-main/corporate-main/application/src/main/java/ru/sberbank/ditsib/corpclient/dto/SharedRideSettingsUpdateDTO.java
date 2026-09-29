package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

/**
 * Информация об изменяемых настройках совместных поездок. Содержит ID и ID элементов настроек
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(
        title = "Информация об изменяемых настройках совместных поездок",
        description = "Информация об изменяемых настройках совместных поездок"
)
@JsonPropertyOrder({ "id" })
public class SharedRideSettingsUpdateDTO extends SharedRideSettingsDTO {
    
    /**
     * Идентификатор настройки
     */
    @Schema(description = "Идентификатор настройки")
    @NotNull
    private UUID id;
    
    /**
     * Словарь "тип настройки - изменяемые элементы настройки"
     */
    @Schema(description = "Словарь \"тип настройки - изменяемые элементы настройки\"")
    private Map<@NotNull String, @Valid SharedRideSettingsItemUpdateDTO> settings;
}
